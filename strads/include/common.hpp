// Copyright (c) 2014, Sailing Lab
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// 3. Neither the name of the <ORGANIZATION> nor the names of its contributors
// may be used to endorse or promote products derived from this software
// without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
/**************************************************************
  @author: Jin Kyu Kim (jinkyuk@cs.cmu.edu)

***************************************************************/
#pragma once 

#include <iostream>
#include <stdio.h>
#include <map>
#include <unordered_map>
#include <vector>
#include <iostream>
#include <math.h>
#include <stdlib.h>
#include <gflags/gflags.h>
#include <glog/logging.h>
#include <assert.h>
class sharedctx;
class ngraphctx;
class parameters;
#include "pi/strads-pi.hpp"
#include "pi/sysparam.hpp"

#include "./include/utility.hpp"
#include "ds/dshard.hpp"
#include "scheduler/scheduler.hpp"

#include "coordinator/coordinator.hpp"

void *user_initparams(sysparam *sp);
void *worker_mach(void *);

class ngraphctx;
class sharedctx;
class context;

#include "com/comm.hpp"
#include "com/zmq/zmq-common.hpp"

#define NO_TIMING_LOG

DECLARE_string(filename);

typedef struct{ 
  int events;
  uint64_t event_start[MAX_EVENT_PER_CMD]; // micro second
  uint64_t event_end[MAX_EVENT_PER_CMD];   // micro second  
}commandlog;

class timing_log{
public:
  timing_log(int64_t maxcommand, int mid, int lid, int gid, const char *tlogprefix)
  : m_maxcommand(maxcommand), m_mid(mid), m_lid(lid), m_gid(gid){

#if !defined(NO_TIMING_LOG)
    m_cmdlog = (commandlog *)calloc(sizeof(commandlog), maxcommand);      
    for(int64_t i=0; i<maxcommand; i++){
      m_cmdlog[i].events = -1;
    }
    m_logfn = (char *)calloc(strlen(tlogprefix)+20, sizeof(char));
    strcpy(m_logfn, tlogprefix);
    char tmp[10];
    sprintf(tmp, "%d.timelog", mid);
    strcat(m_logfn, tmp);
    strads_msg(ERR, "@@@@@@@@@ TIMING LOG file name : %s \n", 
	       m_logfn);
    m_fd = (FILE *)fopen(m_logfn, "wt");
    assert(m_fd);
#endif 
  }

  timing_log(void){}  
  void write_cmdevent_start_log(int64_t cmdid, int eventid, uint64_t start){
#if !defined(NO_TIMING_LOG)
    assert(cmdid <= m_maxcommand);
    strads_msg(ERR, "@@@@@ write event start log cmd id %ld   %d   %ld\n", cmdid, eventid, start); 
    m_cmdlog[cmdid].event_start[eventid] = start;
    if(m_cmdlog[cmdid].events < eventid){
      m_cmdlog[cmdid].events = eventid;
    }	
#endif 
  }

  void write_cmdevent_end_log(int64_t cmdid, int eventid, uint64_t end){
#if !defined(NO_TIMING_LOG)
    assert(cmdid <= m_maxcommand);
    strads_msg(ERR, "@@@@@ write event end log %ld %d %ld\n", cmdid, eventid, end); 
    m_cmdlog[cmdid].event_end[eventid] = end;
    //    assert(m_cmdlog[cmdid].events == eventid);     
#endif 
  }

  void flush_hdd(void){
#if !defined(NO_TIMING_LOG)
    uint64_t rbase = 1397119834728351;
    for(int64_t i=0; i < m_maxcommand; i++){
      if(m_cmdlog[i].events >=0){

	for(int j=0; j<m_cmdlog[i].events+1; j++){
	  if(m_cmdlog[i].event_start[j] == 0)
	    continue;

	  fprintf(m_fd, "set object  %ld rect from  %ld, %ld to %ld, %ld\n", 		  
		  //	  i, m_cmdlog[i].event_start[j] -rbase, i*10, m_cmdlog[i].event_end[j]-rbase, i*10 + 5);
		  i*50 + j, m_cmdlog[i].event_start[j]-rbase, i*50 + j, m_cmdlog[i].event_end[j]-rbase, i*50 + j + 1);
	  int idx = i*50 + j + 1;
	  int color;
	  if(idx%50 == 0){
	    color=8;
	  }else if(idx % 20 == 0){
	    color = 9;
	  }else{	   
	    color = idx%10;
	  }

	  fprintf(m_fd, "set object  %ld front lw 1.0 fc  lt %d fillstyle solid 1.00 border lt -1\n", i*50 + j, color);	    
	}
      }
    }
#endif 
  }

private:
  int64_t m_maxcommand;
  commandlog *m_cmdlog;
  int m_mid;
  int m_lid;
  int m_gid;
  char *m_logfn;
  FILE *m_fd;
};


typedef struct{
  int64_t iteration1;
  int64_t iteration2;
  uint64_t elapsedtime;
  double object;
}logentry;

typedef struct{
  uint64_t stime;
  uint64_t etime;
}timelog;

class timers{
public:
  timers(int timercnt, int eventcnt): m_timer_cnt(timercnt), m_automode(false), m_event_cnt(eventcnt){
    m_logs = (timelog *)calloc(m_timer_cnt, sizeof(timelog));
    memset(m_logs, 0, m_timer_cnt*sizeof(timelog));
    m_free_entry = 0;
    m_elapsedtime = (uint64_t *)calloc(m_event_cnt, sizeof(uint64_t));  // it should be calloc 
    memset(m_elapsedtime, 0, m_event_cnt*sizeof(uint64_t));
  }

  ~timers(){
    free(m_logs);
  }

  uint64_t set_stimer(int idx){
    assert(m_automode == false);
    m_logs[idx].stime = timenow();
    return m_logs[idx].stime;
  }

  uint64_t set_etimer(int idx){
    assert(m_automode == false);
    m_logs[idx].etime = timenow();
    return m_logs[idx].etime;
  }

  uint64_t get_elapsedtime(int idx){ return (m_logs[idx].etime - m_logs[idx].stime); }

  uint64_t set_auto_stimer(void){
    assert(m_automode == true);
    m_logs[m_free_entry].stime = timenow();
    return m_logs[m_free_entry].stime;
  }

  uint64_t set_auto_etimer(void){
    assert(m_automode == true);
    m_logs[m_free_entry].etime = timenow();
    uint64_t ret = m_logs[m_free_entry].etime;
    m_free_entry ++;
    return ret;
  }

  void update_elapsedtime(int start, int end){
    assert(start >= 0 && start <= m_event_cnt-1);
    assert(end >= 0 && end <= m_event_cnt-1);
    assert(end >= start);
    for(int i=start; i <= end; i++){
      m_elapsedtime[i] += (m_logs[i].etime - m_logs[i].stime); 
    }
  }

  void print_elapsedtime(int start, int end){
    assert(start >= 0 && start <= m_event_cnt-1);
    assert(end >= 0 && end <= m_event_cnt-1);
    assert(end >= start);
    for(int i=start; i <= end; i++){
      strads_msg(INF, "\t\tEvent [%d] elapsed time: %lf milli-second \n", 
		 i, m_elapsedtime[i] / 1000.0); 
      m_elapsedtime[i] = 0;
    }    
  }

  void print_elapsedtime(int start, int end, int id){
    assert(start >= 0 && start <= m_event_cnt-1);
    assert(end >= 0 && end <= m_event_cnt-1);
    assert(end >= start);
    for(int i=start; i <= end; i++){
      strads_msg(INF, "\t\t\t\tThid(%d) Event [%d] elapsed time: %lf milli-second \n", 
		 id, i, m_elapsedtime[i] / 1000.0); 
      m_elapsedtime[i] = 0;
    }    
  }
  
private:
  int m_timer_cnt;
  timelog *m_logs;
  int m_free_entry;
  const bool m_automode;
  uint64_t *m_elapsedtime;
  int m_event_cnt;
};


class parameters{
public:

  parameters(sysparam *sp)
    : m_sp(sp),  m_avail(true){
  }

  parameters()
    : m_sp(NULL),  m_avail(false){
  }
  ~parameters(){   
  }
  void print_allparams(void){
    //    strads_msg(INF, "---------------  user    parameters ----------------- \n");
    //    m_up->print();
    //    strads_msg(INF, "---------------  system  parameters ----------------- \n");
    //    m_sp->print();
    //    strads_msg(INF, "---------------  machine parameters ----------------- \n");
    //    m_machspec->print();
    strads_msg(INF, "---------------  End of  parameters ----------------- \n");
  }
  sysparam *m_sp;
private:
  bool m_avail;
};

class task_assignment{

public:
  std::map<int, range *>schmach_tmap;
  std::map<int, range *>schthrd_tmap;
};

typedef struct {
  int64_t size;
  int64_t phaseno;
  uobj_type  type; // user defined type that allows user to parse the data entry 
  void *data;
}staleinfo;

#include "ds/dshard.hpp"

// this class does not have declaration of destructor since it will not be deleted as far as the program runs 
class sharedctx {

public:
  sharedctx(const int r):
    rank(r), m_mrole(mrole_unknown), m_sport_lock(PTHREAD_MUTEX_INITIALIZER), m_rport_lock(PTHREAD_MUTEX_INITIALIZER), 
    m_sport_flag(false), m_rport_flag(false), ring_sport(NULL), ring_rport(NULL), m_fsport_lock(PTHREAD_MUTEX_INITIALIZER), 
    m_frport_lock(PTHREAD_MUTEX_INITIALIZER), m_fsport_flag(false), m_frport_flag(false), ring_fsport(NULL), ring_frport(NULL), 
    m_starsport_lock(PTHREAD_MUTEX_INITIALIZER), m_starrport_lock(PTHREAD_MUTEX_INITIALIZER),  m_starsend(PTHREAD_MUTEX_INITIALIZER),
    m_starrecv(PTHREAD_MUTEX_INITIALIZER), m_params(NULL), m_idvalp_buf(NULL), m_freethrds_lock(PTHREAD_MUTEX_INITIALIZER), 
    m_freethrds(0), m_maxlog(0), m_entries(NULL), m_logfd(NULL), m_betafd(NULL), m_metafd(NULL), m_model_permute(NULL), 
    m_min(HUGE_VAL), m_max(-HUGE_VAL),  m_ready_msgcollection(true) {

    m_scheduler_mid = -1; 
    m_worker_mid = -1;    
    m_coordinator_mid = -1; 
    m_mbuffers = (mbuffer **)calloc(sizeof(mbuffer *), MAX_MACHINES*10);
  };

  sharedctx():
    rank(-1){};

  std::map<int, mnode *> nodes; // machine nodes
  std::map<int, mlink *> links; // node graph links
  std::map<int, mlink *> rlinks; // node graph links
  int rank;                     // machine id
  mach_role m_mrole;

  void *(*machine_func)(void *); 

  void set_sport_flag(bool flag){
    pthread_mutex_lock(&m_sport_lock);
    m_sport_flag = flag;
    pthread_mutex_unlock(&m_sport_lock);
  }

  void set_rport_flag(bool flag){
    pthread_mutex_lock(&m_rport_lock);
    m_rport_flag = flag;
    pthread_mutex_unlock(&m_rport_lock);
  }

  bool get_rport_flag(void){
    bool ret;
    pthread_mutex_lock(&m_rport_lock);
    ret = m_rport_flag;
    pthread_mutex_unlock(&m_rport_lock);
    return ret;
  }

  bool get_sport_flag(void){
    bool ret;
    pthread_mutex_lock(&m_sport_lock);
    ret = m_sport_flag;
    pthread_mutex_unlock(&m_sport_lock);
    return ret;
  }

  void set_rport(_ringport *rport){
    pthread_mutex_lock(&m_rport_lock);
    ring_rport = rport;
    pthread_mutex_unlock(&m_rport_lock);   
  }

  void set_sport(_ringport *sport){
    pthread_mutex_lock(&m_sport_lock);
    ring_sport = sport;
    pthread_mutex_unlock(&m_sport_lock);   
  }

  // fast ring stuff ....
  void set_fsport_flag(bool flag){
    pthread_mutex_lock(&m_fsport_lock);
    m_fsport_flag = flag;
    pthread_mutex_unlock(&m_fsport_lock);
  }

  void set_frport_flag(bool flag){
    pthread_mutex_lock(&m_frport_lock);
    m_frport_flag = flag;
    pthread_mutex_unlock(&m_frport_lock);
  }

  bool get_frport_flag(void){
    bool ret;
    pthread_mutex_lock(&m_frport_lock);
    ret = m_frport_flag;
    pthread_mutex_unlock(&m_frport_lock);
    return ret;
  }

  bool get_fsport_flag(void){
    bool ret;
    pthread_mutex_lock(&m_fsport_lock);
    ret = m_fsport_flag;
    pthread_mutex_unlock(&m_fsport_lock);
    return ret;
  }

  void set_frport(_ringport *rport){
    pthread_mutex_lock(&m_frport_lock);
    ring_frport = rport;
    pthread_mutex_unlock(&m_frport_lock);   
  }

  void set_fsport(_ringport *sport){
    pthread_mutex_lock(&m_fsport_lock);
    ring_fsport = sport;
    pthread_mutex_unlock(&m_fsport_lock);   
  }

  void insert_star_recvport(uint16_t port, _ringport *ctx){
    int r = pthread_mutex_lock(&m_starrecv);
    checkResults("[insert star recvport] get lock failed", r);
    star_recvportmap.insert(std::pair<uint16_t, _ringport *>(port, ctx));
    r = pthread_mutex_unlock(&m_starrecv);   
    checkResults("[insert star recvport] release lock failed", r);
  }

  void insert_star_sendport(uint16_t port, _ringport *ctx){
    int r = pthread_mutex_lock(&m_starsend);
    checkResults("[insert star sendport] release lock failed", r);
    star_sendportmap.insert(std::pair<uint16_t, _ringport *>(port, ctx));
    pthread_mutex_unlock(&m_starsend);   
    checkResults("[insert star sendport] release lock failed", r);
  }

  unsigned int get_size_recvport(void){
    unsigned int ret;
    int r = pthread_mutex_lock(&m_starrecv);
    checkResults("[insert star recvport] get lock failed", r);
    ret = star_recvportmap.size();
    r = pthread_mutex_unlock(&m_starrecv);   
    checkResults("[insert star recvport] release lock failed", r);
    return ret;
  }

  unsigned int get_size_sendport(void){
    unsigned int ret;
    int r = pthread_mutex_lock(&m_starsend);
    checkResults("[insert star sendport] get lock failed", r);
    ret = star_sendportmap.size();
    r = pthread_mutex_unlock(&m_starsend);   
    checkResults("[insert star sendport] release lock failed", r);
    return ret;
  }

  void get_lock_sendportmap(void){
    int r = pthread_mutex_lock(&m_starsend);
    checkResults("[get lock on sendport] get lock failed", r);
  }

  void release_lock_sendportmap(void){
    int r = pthread_mutex_unlock(&m_starsend);
    checkResults("[release lock sendport] unlock failed", r);
  }

  void get_lock_recvportmap(void){
    int r = pthread_mutex_lock(&m_starrecv);
    checkResults("[get lock on recvport] get lock failed", r);
  }

  void release_lock_recvportmap(void){
    int r = pthread_mutex_unlock(&m_starrecv);
    checkResults("[release lock on recv port] unlock failed", r);
  }


  void bind_params(parameters *param){
    m_params = param;
  }

  void make_scheduling_taskpartition(int64_t modelsize, int schedmach, int thrd_permach){

    int parts = schedmach*thrd_permach;   
    int64_t share = modelsize/parts;
    int64_t remain = modelsize % parts;
    int64_t start, end, myshare;

    //  give global assignment scheme per thread 
    for(int i=0; i < parts; i++){
      if(i==0){
	start = 0;
      }else{
	start = m_tmap.schthrd_tmap[i-1]->end + 1;
      }
      if(i < remain){
	myshare = share+1;
      }else{
	myshare = share;
      }
      end = start + myshare -1;
      if(end >= modelsize){
	end = modelsize -1;
      }
      range *tmp = new range;
      tmp->start = start;
      tmp->end = end;
      m_tmap.schthrd_tmap.insert(std::pair<int, range *>(i, tmp));			      
    }
    
    // give global assignment per mach 
    for(int i=0; i < schedmach; i++){
      int start_thrd = i*thrd_permach;
      int end_thrd = start_thrd + thrd_permach - 1;
      range *tmp = new range;
      tmp->start = m_tmap.schthrd_tmap[start_thrd]->start;
      tmp->end = m_tmap.schthrd_tmap[end_thrd]->end;
      m_tmap.schmach_tmap.insert(std::pair<int, range *>(i, tmp));
    }

    if(rank == 0){
      for(auto p : m_tmap.schthrd_tmap){
	strads_msg(ERR, "[scheduling task partitioning] scheduler thread(%d) start(%ld) end(%ld)\n", 
		   p.first, p.second->start, p.second->end); 
      }

      for(auto p : m_tmap.schmach_tmap){
	strads_msg(ERR, "[scheduling task partitioning] scheduler mach(%d) start(%ld) end(%ld)\n", 
		   p.first, p.second->start, p.second->end); 
      }
    }
  }

  mach_role find_role(int mpi_size){

    mach_role mrole = mrole_unknown;
    int sched_machines = m_params->m_sp->m_schedulers;      
    int first_schedmach = mpi_size - sched_machines - 1;
    int first_coordinatormach = mpi_size - 1; 
    if(rank == first_coordinatormach){
      mrole = mrole_coordinator; 
    }else if(rank < first_schedmach){       
      mrole = mrole_worker;
    }else if(rank >= first_schedmach && rank < first_coordinatormach){
      mrole = mrole_scheduler;
    }
    assert(mrole != mrole_unknown);
    return mrole;
  }

  mach_role find_role(int mpi_size, int dstnode){

    mach_role mrole = mrole_unknown;
    int sched_machines = m_params->m_sp->m_schedulers;      
    int first_schedmach = mpi_size - sched_machines - 1;
    int first_coordinatormach = mpi_size - 1; 
    if(dstnode == first_coordinatormach){
      mrole = mrole_coordinator; 
    }else if(dstnode < first_schedmach){       
      mrole = mrole_worker;
    }else if(dstnode >= first_schedmach && dstnode < first_coordinatormach){
      mrole = mrole_scheduler;
    }
    assert(mrole != mrole_unknown);
    return mrole;
  }

  void prepare_machine(int mpi_size){
    assert(rank < mpi_size); // sanity check
    m_mpi_size = mpi_size;

#if defined(SVM_COL_PART)
    //    m_weights = (double *)calloc(m_params->m_up->m_samples, sizeof(double));
    //    m_weights_size = m_params->m_up->m_samples;
#endif 

    if(!m_params->m_sp->m_scheduling){
      assert(m_params->m_sp->m_schedulers == 0);
    }else{
      assert(m_params->m_sp->m_schedulers > 0);
    }

    /* assign roles to machines 
     *   first n machines are workers 
     *   next m machines are schedulers if any. this could be zero if scheduling is disabled. 
     *   last one is coordinator
     *  n + m + 1 == mpi_size 
     */
    m_sched_machines = m_params->m_sp->m_schedulers;      
    m_first_schedmach = mpi_size - m_sched_machines - 1;
    m_worker_machines = m_first_schedmach;
    m_first_workermach = 0;
    m_first_coordinatormach = mpi_size - 1; 

    if(rank == m_first_coordinatormach){
      m_mrole = mrole_coordinator; 
      machine_func = &coordinator_mach;
      m_coordinator_mid = rank - m_first_coordinatormach; 

      strads_msg(ERR, "[prepare_machine] rank(%d) coordinator\n", rank);
      // TODO : fill out scheduler/worekr - send/recvportmap 
    }else if(rank < m_first_schedmach){       
      m_mrole = mrole_worker;
      machine_func = &worker_mach;
      m_worker_mid = rank - m_first_workermach; 
      strads_msg(ERR, "[prepare_machine] rank(%d) worker\n", rank);
      // TODO : fill out coordinator send/recvportmap 
    }else if(rank >= m_first_schedmach && rank < m_first_coordinatormach){
      m_mrole = mrole_scheduler;
      machine_func = &scheduler_mach;
      m_scheduler_mid = rank - m_first_schedmach; 
      strads_msg(ERR, "[prepare_machine] rank(%d) scheduler\n", rank);
      // TODO : fill out coordinator send/recvportmap 
    }
    assert(m_mrole != mrole_unknown);
    make_scheduling_taskpartition(m_params->m_sp->m_modelsize, m_sched_machines, m_params->m_sp->m_thrds_per_scheduler);  
    // give global view of scheduling task partition per thread, per machine

    alloc_idvalp_buf(m_params->m_sp->m_maxset); // for all machines, allocate a buffer of idval_pair type 

    if(m_mrole == mrole_coordinator){ // only coordinator need to make a log for beta, progress log, meta log files 
      assert(m_params != NULL);
      int64_t iterations = m_params->m_sp->m_iter;
      int64_t logfreq = m_params->m_sp->m_logfreq;
      int64_t logs = iterations / logfreq;
      if(iterations % logfreq != 0){
	logs++;
      }
      init_log(logs, m_params);
    }

    m_tloghandler = new timing_log(m_params->m_sp->m_iter*10, rank, 0, 0, m_params->m_sp->m_tlogprefix);    
    make_model_permute(m_params->m_sp->m_modelsize);
  }

  void make_model_permute(int64_t modelsize){
    m_model_permute = (int64_t *)calloc(modelsize, sizeof(int64_t));
    assert(m_model_permute);
    assert(modelsize <= RAND_MAX);

    for(int64_t i=0; i<modelsize; i++){
      m_model_permute[i] = i;
    }
    
    util_permute_fixedorder(m_model_permute, modelsize);

    for(int64_t i=0; i<5; i++){
      strads_msg(ERR, " Rank(%d) m_model_permute[%ld] = %ld \n", rank, i, m_model_permute[i]);
    }

    for(int64_t i=modelsize-5; i<modelsize; i++){
      strads_msg(ERR, " Rank(%d) m_model_permute[%ld] = %ld \n", rank, i, m_model_permute[i]);
    }

  }

  void start_framework(void){
    pthread_attr_t attr;
    pthread_t pthid;
    int rc = pthread_attr_init(&attr);
    checkResults("pthread attr init failed\n", rc);
    rc = pthread_create(&pthid, &attr, machine_func, (void *)this);
    checkResults("pthread_create failed\n", rc);
    void *res;
    pthread_join(pthid, &res);
  }

  void alloc_idvalp_buf(int64_t maxset){
    assert(!m_idvalp_buf);
    assert(maxset >0);
    m_idvalp_buf = (idval_pair *)calloc(maxset, sizeof(idval_pair));
  }

  int get_mpi_size(void){ return m_mpi_size; }

  // data ring stuff
  pthread_mutex_t m_sport_lock; // for flag, not for port
  pthread_mutex_t m_rport_lock; // for flag, not for port
  bool m_sport_flag;
  bool m_rport_flag;
  class _ringport *ring_sport; // once initialized, no more change
  class _ringport *ring_rport; // once initialized, no more change

  // fast ring stuff
  pthread_mutex_t m_fsport_lock; // for flag, not for port
  pthread_mutex_t m_frport_lock; // for flag, not for port
  bool m_fsport_flag;
  bool m_frport_flag;
  class _ringport *ring_fsport; // once initialized, no more change
  class _ringport *ring_frport; // once initialized, no more change

  // star topology stuff -- can be used for any other topology as well. 
  pthread_mutex_t m_starsport_lock; // for flag, not for port
  pthread_mutex_t m_starrport_lock; // for flag, not for port
  bool m_starsport_flag;
  bool m_starrport_flag;
  pthread_mutex_t m_starsend; // for flag, not for port
  pthread_mutex_t m_starrecv; // for flag, not for port
  std::map<uint16_t, _ringport *> star_recvportmap;
  std::map<uint16_t, _ringport *> star_sendportmap;

  parameters *m_params;

  int m_scheduler_mid; // scheduler id from 0 to sizeof(schedulers)-1
  int m_worker_mid;    // worker id from 0 to sizeof(workers)-1
  int m_coordinator_mid; // coordinator id from 0 to sizeof(coordinator)-1

  task_assignment m_tmap;

  int m_sched_machines;
  int m_first_schedmach;
  int m_worker_machines;
  int m_first_workermach;
  int m_first_coordinatormach; // first one and last one since only one coordinator now
  int m_mpi_size;

  // rank number - ringport map 
  std::map<int, _ringport *> scheduler_sendportmap; // coordinator
  std::map<int, _ringport *> scheduler_recvportmap; // coordinator 
  
  std::map<int, _ringport *> coordinator_sendportmap; // workers and scheduler 
  std::map<int, _ringport *> coordinator_recvportmap; // workers and scheduler 

  // star topology only: ring topology does not use this map 
  std::map<int, _ringport *> worker_sendportmap; // coordinator
  std::map<int, _ringport *> worker_recvportmap; // coordinator 

  idval_pair *m_idvalp_buf;
  
  void register_shard(dshardctx *ds){
    std::string *alias = new std::string(ds->m_alias);
    //    strads_msg(ERR, "MALIAS : %c\n", ds->m_alias[0]);
    //    std::string alias("aa");
    std::cout<< "-- SHARD ALIAS " << alias << std::endl;
    m_shardmap.insert(std::pair<std::string, dshardctx *>(*alias, ds));
  }

  dshardctx *get_dshard_with_alias(std::string &alias){
    dshardctx *ret = NULL;
    auto p = m_shardmap.find(alias);
    if(p == m_shardmap.end()){
      ret=NULL;
    }else{
      ret = p->second;
    }
    return ret;    
  }

  std::map<std::string, dshardctx *>m_shardmap; // alias and dshardctx mapping
  std::map<int64_t, staleinfo *>m_stale;

  int64_t get_freethrdscnt(void){
    int64_t ret;
    pthread_mutex_lock(&m_freethrds_lock);
    ret = m_freethrds;
    pthread_mutex_unlock(&m_freethrds_lock);
    return ret;
  }

  pthread_mutex_t m_freethrds_lock;
  int64_t m_freethrds;

  void init_log(int64_t maxlog, parameters *params){
    assert(m_entries == NULL); //otherwise, it already initialized. it's fatal error 
    m_maxlog = maxlog;
    m_entries = (logentry *)calloc(maxlog, sizeof(logentry));    
    for(int64_t i=0; i < maxlog; i++){
      m_entries[i].iteration1 = INVALID_LOGID; // don't forget 
      m_entries[i].iteration2 = INVALID_LOGID; // don't forget 
    }
    assert(m_entries);
    time_t now = time(0);
    tm *localtm = localtime(&now);
    strads_msg(INF, " local date and time : %s \n", asctime(localtm));
    char *tmplogfn = util_convert_date_to_fn(asctime(localtm));
    strads_msg(INF, "FN FILE NAME : %s \n", tmplogfn);
    int tlen = strlen(tmplogfn) + strlen(params->m_sp->m_logdir);

    // auto GetFlag = gflags::GetCommandLineFlagInfoOrDie;
    std::string inpfile = FLAGS_filename; //*(std::string*) GetFlag("filename").flag_ptr;

    char *inputfn = util_path2string(inpfile.c_str());
    char *postfix = (char *)calloc(strlen(inputfn)+1000, sizeof(char));
    sprintf(postfix, "%s_w%03d_thrd%03d_s%03d_thrd%03d_maxset%04ld_threshold%lf", 
	    inputfn, 
	    m_worker_machines, 
	    m_params->m_sp->m_thrds_per_worker,
	    m_sched_machines, 
	    m_params->m_sp->m_thrds_per_scheduler,
	    m_params->m_sp->m_maxset, 
	    //	    m_params->m_sp->m_beta, 
	    m_params->m_sp->m_infthreshold);    
    
    char *fn = (char *)calloc(tlen + strlen(postfix)+1024, sizeof(char));
    strcpy(fn, params->m_sp->m_logdir);
    strcat(fn, tmplogfn);
    strcat(fn, postfix);


    strads_msg(ERR, "LOG FILE NAME : %s strlen(fn): %ld\n", fn, strlen(fn));


    char *logfn = (char *)calloc(strlen(fn)+1000, sizeof(char));
    char *betafn = (char *)calloc(strlen(fn)+1000, sizeof(char));
    char *metafn = (char *)calloc(strlen(fn)+1000, sizeof(char));
    strcpy(logfn, fn);
    strcpy(betafn, fn);
    strcpy(metafn, fn);

#if defined(NO_INTERFERENCE_CHECK)

#if defined(NO_WEIGHT_SAMPLING)
    strads_msg(ERR, "NO SCHEDULING , NO DEPENDENCY CHECKING \n");
    strcat(logfn, ".nocheck.nosched.log");
#else
    strads_msg(ERR, "YES SCHEDULING , NO DEPENDENCY CHECKING \n");
    strcat(logfn, ".nocheck.sched.log");
#endif

#else

#if defined(NO_WEIGHT_SAMPLING)
    strads_msg(ERR, "NO SCHEDULING , YES DEPENDENCY CHECKING \n");
    strcat(logfn, ".check.nosched.log");
#else 
    strads_msg(ERR, "YES SCHEDULING , YES DEPENDENCY CHECKING \n");
    strcat(logfn, ".check.sched.log");
#endif

#endif
    sleep(3);
    strcat(betafn, ".beta");
    strcat(metafn, ".meta");
    strads_msg(ERR, "Meta File : %s\n", metafn);
    strads_msg(ERR, "Beta File : %s\n", betafn);
    strads_msg(ERR, "Log  File : %s\n", logfn);
    m_logfd = (FILE *)fopen(logfn, "wt");
    m_metafd = (FILE *)fopen(metafn, "wt");
    m_betafd = (FILE *)fopen(betafn, "wt");
    assert(m_logfd);
    assert(m_betafd);
    assert(m_metafd);   
  }

  void write_log(int64_t logid, uint64_t elapsedtime, double object){
    assert(m_entries);
    assert(logid >= 0);
    assert(logid < m_maxlog);
    m_entries[logid].iteration1 = logid;
    m_entries[logid].iteration2 = logid;
    m_entries[logid].elapsedtime = elapsedtime;
    m_entries[logid].object = object;

  }


  void write_log(int64_t logid, uint64_t elapsedtime){
    assert(m_entries);
    assert(logid >= 0);
    assert(logid < m_maxlog);
    m_entries[logid].iteration1 = logid;
    m_entries[logid].elapsedtime = elapsedtime;
  }

  void write_log(int64_t logid, double object){
    assert(m_entries);
    assert(logid >= 0);
    assert(logid < m_maxlog);
    m_entries[logid].iteration2 = logid;
    m_entries[logid].object = object;

    fprintf(m_logfd, " %ld  %lf %lf \n", 
	    m_entries[logid].iteration2, 
	    m_entries[logid].elapsedtime/ 1000000.0, 
	    m_entries[logid].object);

    fflush(m_logfd);
  }

  void write_log_total(int64_t logid, double object, int64_t updateparamsofar, int64_t validcnt, double totaldelta){
    assert(m_entries);
    assert(logid >= 0);
    assert(logid < m_maxlog);
    m_entries[logid].iteration2 = logid;
    m_entries[logid].object = object;

    fprintf(m_logfd, " %ld  %lf %lf %ld %ld %lf\n", 
	    m_entries[logid].iteration2, 
	    m_entries[logid].elapsedtime/ 1000000.0, 
	    m_entries[logid].object, 	   
	    updateparamsofar, 
	    validcnt, 
	    totaldelta);


    fflush(m_logfd);

    strads_msg(ERR, "\n\t\t\t\t %ld elapsed %lf obj %lf updated_params  %ld   %ld    %lf\n", 
	       m_entries[logid].iteration2, 
	       m_entries[logid].elapsedtime/ 1000000.0, 
	       m_entries[logid].object, 
	       updateparamsofar, 
	       validcnt, 
	       totaldelta);

  }

  void flush_log(void){
#if 0 
    assert(m_logfd);
    for(int64_t i=0; i < m_maxlog; i++){

      assert(m_entries[i].iteration1 == m_entries[i].iteration2);
      assert(m_entries[i].iteration1 != INVALID_LOGID);
      assert(m_entries[i].iteration2 != INVALID_LOGID);

      fprintf(m_logfd, " %ld  %lf %lf \n", 
	      m_entries[i].iteration1,
	      m_entries[i].elapsedtime / 1000000.0,  // mill seconds 
	      m_entries[i].object);	      
    }
#endif
    fclose(m_logfd);
  }

  mbuffer **make_msgcollection(void){
    assert(m_ready_msgcollection);
    m_ready_msgcollection = false;
    for(int i=0; i < m_worker_machines; i++){
      while(1){
	void *buf = worker_recvportmap[i]->ctx->pull_entry_inq();
	if(buf != NULL){
          m_mbuffers[i] = (mbuffer *)buf;
          break;
        }
      }
    }
    return m_mbuffers;
  }
  void release_msgcollection(mbuffer **bufs){
    assert(!m_ready_msgcollection);
    for(int i=0; i < m_worker_machines; i++){    
      worker_recvportmap[i]->ctx->release_buffer((void *)bufs[i]);
    }
    m_ready_msgcollection = true;
  }

  int64_t m_maxlog;  
  logentry *m_entries;
  FILE *m_logfd;
  FILE *m_betafd;
  FILE *m_metafd;
  int64_t *m_model_permute;
  timing_log *m_tloghandler;
  double m_min;  // TODO : SVM specific code , get rid of them when you finish app specific ds 
  double m_max;  // TODO : SVM specific code , get rid of them when you finish app specific ds 
  bool m_ready_msgcollection;
  mbuffer **m_mbuffers;
}; // sharectx 

class ngraphctx {
public:
  std::map<int, stnode *> nodes; // mapping between MPI rank and stnodes 
}; // node graph context 

#define CMDSTATE_PENDING (0x10)
#define CMDSTATE_DONE (0x20)
#define CMDSTATE_NOT_ASSIGNED (0x0)
// command was delivered to worker(s), and done ack arrived

enum cmdstatus { CMD_UNCOMPLETE=100, CMD_COMPLETE=200 };

typedef struct{
  long cmdid;
  machtype mtype;
  int receivers;
  int base;
  char machines[MAX_MACHINES];  
  int pendingmach;
  cmdstatus status; 
}cmdentry;

// pendingpacket management 
class ppacketmgt{

public:
  ppacketmgt(int maxsysc, int maxuserc, int firstsched, int schedmach, int firstworker, int workermach)
    : m_max_scmd(maxsysc), m_max_ucmd(maxuserc), m_firstsched(firstsched), m_schedmach(schedmach), 
      m_firstworker(firstworker), m_workermach(workermach), m_cmdclock(0){
    m_syscmdq_lock = PTHREAD_MUTEX_INITIALIZER;
    m_usercmdq_lock = PTHREAD_MUTEX_INITIALIZER;
  }

  ppacketmgt(int maxsysc, int maxuserc, class sharedctx &ctx)
    : m_max_scmd(maxsysc), m_max_ucmd(maxuserc), m_firstsched(ctx.m_first_schedmach), 
      m_schedmach(ctx.m_sched_machines), m_firstworker(ctx.m_first_workermach), 
      m_workermach(ctx.m_worker_machines), m_cmdclock(0){
    m_syscmdq_lock = PTHREAD_MUTEX_INITIALIZER;
    m_usercmdq_lock = PTHREAD_MUTEX_INITIALIZER;
  }

  void push_cmdq(long cmdid, machtype mtype){    
    cmdentry *entry = (cmdentry *)calloc(1, sizeof(cmdentry));
    // CAVEAT: entry should be initialized to zero since NOT_ASSIGNED status is defined as 0x0
    int base=-1, receivers=-1;
    entry->cmdid = cmdid;    
    if(mtype == m_worker){
      base = m_firstworker;
      receivers = m_workermach;     
    }else if(mtype == m_scheduler){
      base = m_firstsched;
      receivers = m_schedmach;     
    }   
    assert(base>=0);
    assert(receivers>0);

    entry->receivers = receivers;
    entry->base = base;

    for(int i=0; i < receivers; i++){
      entry->machines[base + i]=CMDSTATE_PENDING;
    }
    entry->status = CMD_UNCOMPLETE;
    entry->pendingmach = receivers;
    entry->mtype = mtype;

    int rc = pthread_mutex_lock(&m_syscmdq_lock);
    checkResults("[push cmdq] get lock failed", rc);
    m_syscmdpendq.insert(std::pair<long, cmdentry *>(cmdid, entry));
    rc = pthread_mutex_unlock(&m_syscmdq_lock);
    checkResults("[push cmdq] release lock failed", rc);
  }

  long mark_oneack(mbuffer *mbuf){
    long cmdid = mbuf->cmdid;    
    long src = mbuf->src_rank; // rank number

    int rc = pthread_mutex_lock(&m_syscmdq_lock);
    checkResults("[push mark_oneack] get lock failed", rc);
    std::unordered_map<long, cmdentry *>::iterator it = m_syscmdpendq.find(cmdid);
    if(it == m_syscmdpendq.end()){
      LOG(FATAL) << "Fatal: cmdid " << cmdid << " is not found in the pending command q" << std::endl;
    }
    cmdentry *entry = it->second;

    strads_msg(ERR, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ cmdid(%ld) src(%ld)\n", cmdid, src);

    assert(entry->machines[src] == CMDSTATE_PENDING);

    entry->machines[src] = CMDSTATE_DONE;
    entry->pendingmach--; 
    assert(entry->pendingmach >= 0);    

    if(entry->pendingmach == 0){
      entry->status = CMD_COMPLETE;
      m_syscmdpendq.erase(cmdid);
      m_syscmddoneq.insert(std::pair<long, cmdentry *>(cmdid, entry));     
    }

    rc = pthread_mutex_unlock(&m_syscmdq_lock);
    checkResults("[push mark_oneack] release lock failed", rc);   
    return cmdid;
  }

  // free entry that was done and checked here 
  bool check_cmddone(long cmdid){
    bool ret;
    int rc = pthread_mutex_lock(&m_syscmdq_lock);
    checkResults("[push mark_oneack] get lock failed", rc);
    std::unordered_map<long, cmdentry *>::iterator it = m_syscmddoneq.find(cmdid);    
    if(it != m_syscmddoneq.end()){
      ret = true;
      m_syscmddoneq.erase(cmdid);
      free(it->second); 
      // this is matching with calloc above 
      // if replaced with pool, this wil be replaced with release pool entry 
    }else{
      ret = false;
    }
    rc = pthread_mutex_unlock(&m_syscmdq_lock);
    checkResults("[push mark_oneack] get lock failed", rc);
    return ret;
  }


  long get_cmdclock(void){
    int rc = pthread_mutex_lock(&m_syscmdq_lock);
    checkResults("[push mark_oneack] get lock failed", rc);
    long cmdclock = m_cmdclock;
    m_cmdclock++; // for next call 
    rc = pthread_mutex_unlock(&m_syscmdq_lock);
    checkResults("[push mark_oneack] get lock failed", rc);
    return cmdclock;
  }

  void print_syscmdpendq(void){
    strads_msg(ERR, "[@@@@@@@ SYS_PENDQ] size(%ld)\n", m_syscmdpendq.size());     

    int rc = pthread_mutex_lock(&m_syscmdq_lock);
    checkResults("[print syscmd pendq ] get lock failed", rc);
    for(auto p: m_syscmdpendq){
      long id = p.first;
      cmdentry *entry = p.second;
      assert(entry->cmdid == id);           
      strads_msg(ERR, "[@@@@@@@ SYS_PENDQ] cmdid(%ld) mtype(%d) recv(%d) base(%d) pmach(%d) cmdstate(%d)\n", 
		 entry->cmdid, entry->mtype, entry->receivers, entry->base,entry->pendingmach, entry->status);      
      for(int i=0; i < entry->receivers; i++){
	strads_msg(ERR, " pendq_mach[%d]=(%d) ", i+entry->base, entry->machines[i+entry->base]); 
      }
    }
    rc = pthread_mutex_unlock(&m_syscmdq_lock);
    checkResults("[print syscmd pendq ] release lock failed", rc);
  }

  void print_syscmddoneq(void){
    strads_msg(ERR, "[@@@@@@@ SYS_DONEQ] size(%ld)\n", m_syscmddoneq.size());     
    int rc = pthread_mutex_lock(&m_syscmdq_lock);
    checkResults("[print syscmd doneq ] get lock failed", rc);
    for(auto p: m_syscmddoneq){
      long id = p.first;
      cmdentry *entry = p.second;
      assert(entry->cmdid == id);           
      strads_msg(ERR, "[@@@@@@@ SYS_DONEQ] cmdid(%ld) mtype(%d) recv(%d) base(%d) pmach(%d) cmdstate(%d)\n", 
		 entry->cmdid, entry->mtype, entry->receivers, entry->base,entry->pendingmach, entry->status);      
      for(int i=0; i < entry->receivers; i++){
	strads_msg(ERR, " doneq_mach[%d]=(%d) ", i+entry->base, entry->machines[i+entry->base]); 
      }
    }
    rc = pthread_mutex_unlock(&m_syscmdq_lock);
    checkResults("[print syscmd doneq] release lock failed", rc);
  }

  uint64_t get_syscmddoneq_size(void){
    uint64_t size;
    int rc = pthread_mutex_lock(&m_syscmdq_lock);
    checkResults("[get syscmd doneq size ] get lock failed", rc);
    size = m_syscmddoneq.size();
    rc = pthread_mutex_unlock(&m_syscmdq_lock);
    checkResults("[get syscmd doneq size ] release lock failed", rc);
    return size;
  }

  uint64_t get_syscmdpendq_size(void){
    uint64_t size;
    int rc = pthread_mutex_lock(&m_syscmdq_lock);
    checkResults("[get syscmd doneq size ] get lock failed", rc);
    size = m_syscmdpendq.size();
    rc = pthread_mutex_unlock(&m_syscmdq_lock);
    checkResults("[get syscmd doneq size ] release lock failed", rc);
    return size;
  }
  
private:
  int m_max_scmd; // max limits on the number of pending system command
  int m_max_ucmd; // max limits on the number of pending user command 
    std::unordered_map<long, cmdentry *>m_syscmdpendq;
  std::unordered_map<long, cmdentry *>m_syscmddoneq;
  std::unordered_map<long, cmdentry *>m_usercmdpendq;
  std::unordered_map<long, cmdentry *>m_usercmddoneq;
  int m_firstsched;
  int m_schedmach;
  int m_firstworker;
  int m_workermach;
  long m_cmdclock; // command clock 
  pthread_mutex_t m_syscmdq_lock;
  pthread_mutex_t m_usercmdq_lock;
  // TODO : think about circular scheme 
};

void *process_common_system_cmd(sharedctx *ctx, mbuffer *mbuf, context *recv_ctx, context *send_ctx, bool *tflag);
void mcopy_broadcast_to_workers(sharedctx *ctx, void *tmp, int len);
void mcopy_broadcast_to_workers_objectcalc(sharedctx *ctx, void *tmp, int len);
