package org.olf.erm

import grails.gorm.MultiTenant
import org.olf.general.RefdataValue
import org.olf.kb.Pkg
import org.olf.kb.ErmResource
import org.olf.kb.PackageContentItem
import org.olf.kb.PlatformTitleInstance
import javax.persistence.Transient


/**
 * Entitlement (A description of a right to access a specific digital resource, which can be an 
 * title on a platform (But not listed in a package), a title named in a package, a full package of resources
 *
 * OFTEN attached to an agreement, but it's possible we know we have the right to access a resource
 * without perhaps knowning which agreement controls that right.
 *
 */
public class Entitlement implements MultiTenant<Entitlement> {

  String id

  ErmResource resource

  // The date ranges on which this line item is active. These date ranges allow the system to determine
  // what content is "Live" in an agreement. Content can be "Live" without being switched on, and 
  // vice versa. The dates indicate that we believe the agreement is in force for the items specified.
  // For Trials, these dates will indicate the dates of the trial, for live agreements the agreement item dates
  Date activeFrom
  Date activeTo

  static belongsTo = [
    owner:SubscriptionAgreement
  ]

  static hasMany = [
    coverage: HoldingsCoverage
  ]

  static mappedBy = [
    coverage: 'entitlement'
  ]

  // Allow users to individually switch on or off this content item. If null, should default to the agreement
  // enabled setting. The activeFrom and activeTo dates determine if a content item is "live" or not. This flag
  // determines if we wish live content to be visible to patrons or not. Content can be "Live" but not enabled,
  // although that would be unusual.
  Boolean enabled 
  

  static mapping = {
                   id column: 'ent_id', generator: 'uuid', length:36
              version column: 'ent_version'
                owner column: 'ent_owner_fk'
             resource column: 'ent_resource_fk'
              enabled column: 'ent_enabled'
           activeFrom column: 'ent_active_from'
             activeTo column: 'ent_active_to'
  }


  static constraints = {
        owner(nullable:true,  blank:false)
     resource(nullable:false, blank:false)
      enabled(nullable:true,  blank:false)
   activeFrom(nullable:true,  blank:false)
     activeTo(nullable:true,  blank:false)
  }

  @Transient
  public String getExplanation() {
    
    String result = null
    switch (resource) {
      case { it instanceof Pkg }:
        result = 'Agreement includes a package containing this item'
        break
      case { it instanceof PlatformTitleInstance }:
        result = 'Agremment includes this title directly'
        break
      case { it instanceof PackageContentItem }:
        result = 'Agreement includes this item from a package specifically'
        break
    }

    return result
  }

}
