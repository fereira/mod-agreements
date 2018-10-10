package org.olf.erm

import grails.gorm.MultiTenant
import org.olf.kb.PlatformTitleInstance;

/**
 * A Proxy object that holds information about the Purchase order line in an external acquisitions system
 *
 */
public class ContentActivationRecord implements MultiTenant<ContentActivationRecord> {

  String id
  Date dateActivation
  Date dateDeactivation
  ContentActivationTarget target
  PlatformTitleInstance pti

  static belongsTo = [
  ]

  static hasMany = [
  ]

  static mappedBy = [
  ]

  static mapping = {
           table 'content_activation_target'
                  id column: 'car_id', generator: 'uuid', length:36
             version column: 'car_version'
      dateActivation column: 'car_date_activation'
    dateDeactivation column: 'car_date_deactivation'
              target column: 'car_target_cat_fk'
                 pti column: 'car_pti_fk'
  }

  static constraints = {
  }

}
