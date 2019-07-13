package org.olf.general.jobs
import java.time.Instant

import com.k_int.web.toolkit.refdata.Defaults
import com.k_int.web.toolkit.refdata.RefdataValue

import grails.events.EventPublisher
import grails.gorm.MultiTenant
import grails.gorm.dirty.checking.DirtyCheck
import grails.gorm.multitenancy.Tenants
import grails.util.Holders

@DirtyCheck
abstract class PersistentJob implements MultiTenant<PersistentJob> {
  
  static transients = ['work']
    
  String id
  String name
  
  @Defaults(['Queued', 'In progress', 'Ended'])
  RefdataValue status
  List<LogEntry> logEntries
  Instant dateCreated
  Instant started
  Instant ended
  
  @Defaults(['Success', 'Partial success', 'Failure', 'Interrupted'])
  RefdataValue result
  
  static hasMany = [
    logEntries: LogEntry
  ]
  
  
  static mappedBy = ['logEntries': 'job']
  
  static mapping = {
    tablePerHierarchy false
                   id generator: 'uuid2', length:36
                 name column:'job_name'
               status column:'job_status_fk'
           logEntries cascade: 'all-delete-orphan'
          dateCreated column:'job_date_created'
              started column:'job_started'
                ended column:'job_ended'
               result column:'job_result_fk'
  }

  static constraints = {
            name (nullable:false, blank:false)
          status (nullable:false)
     dateCreated (nullable:true)
         started (nullable:true)
           ended (nullable:true)
          result (nullable:true)
  }
  
  def afterInsert () {
    // Ugly work around events being raised on multi-tenant GORM entities not finding subscribers
    // from the root context.
    JobRunnerService jrs = Holders.applicationContext.getBean('jobRunnerService')
    jrs.handleNewJob(this.id, Tenants.currentId())
  }
  
  List<LogEntry> getErrorEntries() {
    RefdataValue errorType = LogEntry.lookupType('Error')
    LogEntry.findAllByType (errorType)
  }
  
  void begin () {
    this.started = Instant.now()
    this.statusFromString = 'In progress'
    this.save(failOnError: true)
  }
  
  void end () {
    this.ended = Instant.now()
    this.statusFromString = 'Ended'
    if (!result) {
      // If errors then set to partial.
      if (getErrorEntries()) {
        this.resultFromString = 'Partial success'
      } else {
        this.resultFromString = 'Success'
      }
    }
    this.save( failOnError: true, flush:true )
  }
  
  void fail() {
    this.resultFromString = 'Failure'
    end()
  }
  
  void interrupted() {
    this.resultFromString = 'Interrupted'
    end()
  }
  
  abstract Runnable getWork()
  
  String toString() {
    "${name}"
  }
}
