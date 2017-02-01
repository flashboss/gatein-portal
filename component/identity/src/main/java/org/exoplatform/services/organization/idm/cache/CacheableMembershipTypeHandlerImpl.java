/*
 * Copyright (C) 2003-2017 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.organization.idm.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.cache.MembershipCacheKey;
import org.exoplatform.services.organization.cache.OrganizationCacheHandler;
import org.exoplatform.services.organization.idm.MembershipTypeDAOImpl;
import org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl;
import org.exoplatform.services.organization.idm.PicketLinkIDMService;

public class CacheableMembershipTypeHandlerImpl extends MembershipTypeDAOImpl {

  private final ExoCache<String, MembershipType> membershipTypeCache;

  private final ExoCache<Serializable, Membership> membershipCache;

  @SuppressWarnings("unchecked")
  public CacheableMembershipTypeHandlerImpl(OrganizationCacheHandler organizationCacheHandler,
                                            PicketLinkIDMOrganizationServiceImpl orgService,
                                            PicketLinkIDMService service) {
    super(orgService, service);
    this.membershipTypeCache = organizationCacheHandler.getMembershipTypeCache();
    this.membershipCache = organizationCacheHandler.getMembershipCache();
  }

  /**
   * {@inheritDoc}
   */
  public MembershipType findMembershipType(String name) throws Exception {
    MembershipType membershipType = (MembershipType) membershipTypeCache.get(name);
    if (membershipType != null) {
      return membershipType;
    }

    membershipType = super.findMembershipType(name);
    if (membershipType != null) {
      membershipTypeCache.put(name, membershipType);
    }

    return membershipType;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<MembershipType> findMembershipTypes() throws Exception {

    Collection<MembershipType> membershipTypes = super.findMembershipTypes();
    for (MembershipType membershipType : membershipTypes)
      membershipTypeCache.put(membershipType.getName(), membershipType);

    return membershipTypes;
  }

  /**
   * {@inheritDoc}
   */
  public MembershipType removeMembershipType(String name, boolean broadcast) throws Exception {
    MembershipType membershipType = super.removeMembershipType(name, broadcast);
    if (membershipType != null) {
      membershipTypeCache.remove(name);

      List<? extends Membership> memberships = membershipCache.getCachedObjects();
      for (Membership membership : memberships) {
        if (membership.getMembershipType().equals(name)) {
          membershipCache.remove(membership.getId());
          membershipCache.remove(new MembershipCacheKey(membership));
        }
      }
    }

    return membershipType;
  }

  /**
   * {@inheritDoc}
   */
  public MembershipType saveMembershipType(MembershipType mt, boolean broadcast) throws Exception {
    MembershipType membershipType = super.saveMembershipType(mt, broadcast);
    membershipTypeCache.put(membershipType.getName(), membershipType);

    return membershipType;
  }
}
