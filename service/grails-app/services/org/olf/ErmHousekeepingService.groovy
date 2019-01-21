package org.olf

import grails.gorm.multitenancy.Tenants
import org.olf.kb.RemoteKB
import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.Transactional

/**
 * This service works at the module level, it's often called without a tenant context.
 */
@Transactional
public class ErmHousekeepingService {

  /**
   * This is called by the eventing mechanism - There is no web request context
   * there may be something funky to do with events and multi tenant setup.
   */
  @Subscriber('okapi:schema_update')
  public void onSchemaUpdate(tn, tid) {
    log.debug("ErmHousekeepingService::onSchemaUpdate(${tn},${tid})")
    // Skip this until we can work out whats going wrong...
    setupData(tn, tid);
  }

  private void setupData(tenantName, tenantId) {

    log.info("ErmHousekeepingService::setupData(${tenantName},${tenantId})");

    // leaving this here as an example - it works, but we don't actually want to do this in practice!
    Tenants.withId(tenantId) {

      // A special record for packages which are really defined locally - this is an exceptional situation
      RemoteKB local_kb = RemoteKB.findByName('LOCAL') ?: new RemoteKB( name:'LOCAL',
                                                                        rectype: RemoteKB.RECTYPE_PACKAGE,
                                                                        active:Boolean.TRUE,
                                                                        supportsHarvesting:false,
                                                                        activationEnabled:false).save(flush:true, failOnError:true);

      RemoteKB gokb_test = RemoteKB.findByName('GOKb_TEST') ?: new RemoteKB( name:'GOKb_TEST',
                                                                        type:'org.olf.kb.adapters.GOKbOAIAdapter',
                                                                        uri:'http://gokbt.gbv.de/gokb/oai/index/packages',
                                                                        fullPrefix:'gokb',
                                                                        rectype: RemoteKB.RECTYPE_PACKAGE,
                                                                        active:Boolean.TRUE,
                                                                        supportsHarvesting:true,
                                                                        activationEnabled:false).save(flush:true, failOnError:true);

      RemoteKB ebsco_live = RemoteKB.findByName('EBSCO_API') ?: new RemoteKB( name:'EBSCO_LIVE',
                                                                        type:'org.olf.kb.adapters.EbscoKBAdapter',
                                                                        uri:'https://sandbox.ebsco.io',
                                                                        principal:'YOUR_CLIENT_ID',
                                                                        credentials:'YOUR_API_KEY',
                                                                        rectype: RemoteKB.RECTYPE_PACKAGE,
                                                                        active:Boolean.FALSE,
                                                                        supportsHarvesting:false,
                                                                        activationSupported:true,
                                                                        activationEnabled:false).save(flush:true, failOnError:true);


    }
  }
}
