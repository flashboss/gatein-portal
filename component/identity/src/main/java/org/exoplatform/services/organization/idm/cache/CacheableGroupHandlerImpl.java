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

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.cache.MembershipCacheKey;
import org.exoplatform.services.organization.cache.OrganizationCacheHandler;
import org.exoplatform.services.organization.idm.ExtGroup;
import org.exoplatform.services.organization.idm.GroupDAOImpl;
import org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl;
import org.exoplatform.services.organization.idm.PicketLinkIDMService;
import org.exoplatform.services.organization.impl.GroupImpl;

public class CacheableGroupHandlerImpl extends GroupDAOImpl {

  private final ExoCache<String, Group>            groupCache;

  private final ExoCache<Serializable, Membership> membershipCache;

  private final ThreadLocal<Boolean>               disableCacheInThread = new ThreadLocal<>();

  @SuppressWarnings("unchecked")
  public CacheableGroupHandlerImpl(OrganizationCacheHandler organizationCacheHandler,
                                   PicketLinkIDMOrganizationServiceImpl orgService,
                                   PicketLinkIDMService service) {
    super(orgService, service);
    this.groupCache = organizationCacheHandler.getGroupCache();
    this.membershipCache = organizationCacheHandler.getMembershipCache();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addChild(Group parent, Group child, boolean broadcast) throws Exception {
    super.addChild(parent, child, broadcast);
    cacheGroup(child);
  }

  /**
   * {@inheritDoc}
   */
  public Group findGroupById(String groupId) throws Exception {
    Group group = null;
    if (disableCacheInThread.get() == null || !disableCacheInThread.get()) {
      group = (Group) groupCache.get(groupId);
    }
    if (group == null) {
      group = super.findGroupById(groupId);
      if (group != null) {
        cacheGroup(group);
      }
    }
    return group == null ? null : (Group) SerializationUtils.clone((Serializable) group);
  }

  /**
   * {@inheritDoc}
   */
  public Collection<Group> findGroupByMembership(String userName, String membershipType) throws Exception {
    Collection<Group> groups = super.findGroupByMembership(userName, membershipType);

    for (Group group : groups) {
      cacheGroup(group);
    }

    return groups;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<Group> resolveGroupByMembership(String userName, String membershipType) throws Exception {
    Collection<Group> groups = super.resolveGroupByMembership(userName, membershipType);

    for (Group group : groups) {
      cacheGroup(group);
    }

    return groups;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<Group> findGroups(Group parent) throws Exception {
    Collection<Group> groups = super.findGroups(parent);
    for (Group group : groups) {
      cacheGroup(group);
    }

    return groups;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<Group> findGroupsOfUser(String user) throws Exception {
    Collection<Group> groups = super.findGroupsOfUser(user);
    for (Group group : groups) {
      cacheGroup(group);
    }

    return groups;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<Group> getAllGroups() throws Exception {
    Collection<Group> groups = super.getAllGroups();
    for (Group group : groups) {
      cacheGroup(group);
    }
    return groups;
  }

  /**
   * {@inheritDoc}
   */
  public Group removeGroup(Group group, boolean broadcast) throws Exception {
    Group gr = null;
    disableCacheInThread.set(true);
    try {
      gr = super.removeGroup(group, broadcast);

      String groupId = getGroupId(group);
      groupCache.remove(groupId);

      // Delete child groups from cache
      List<? extends Group> cachedGroups = groupCache.getCachedObjects();
      for (Group cachedGroup : cachedGroups) {
        if(cachedGroup.getId().startsWith(groupId + "/")) {
          groupCache.remove(cachedGroup.getId());
        }
      }

      List<? extends Membership> memberships = membershipCache.getCachedObjects();
      for (Membership membership : memberships) {
        if (membership.getGroupId().equals(groupId)) {
          membershipCache.remove(membership.getId());
          membershipCache.remove(new MembershipCacheKey(membership));
        }
      }
    } finally {
      disableCacheInThread.set(false);
    }
    return gr;
  }

  /**
   * {@inheritDoc}
   */
  public void saveGroup(Group group, boolean broadcast) throws Exception {
    super.saveGroup(group, broadcast);
    cacheGroup(group);
  }

  public void clearCache() {
    groupCache.clearCache();
  }

  private String getGroupId(Group group) {
    if (StringUtils.isNotBlank(group.getId())) {
      return group.getId();
    }
    return (StringUtils.isBlank(group.getParentId()) ? "" : group.getParentId()) + "/" + group.getGroupName();
  }

  private void cacheGroup(Group group) {
    String groupId = group.getId();
    if (StringUtils.isBlank(groupId)) {
      groupId = getGroupId(group);
      if (group instanceof ExtGroup) {
        ((ExtGroup) group).setId(groupId);
      } else if (group instanceof GroupImpl) {
        ((GroupImpl) group).setId(groupId);
      }
    }
    groupCache.put(groupId, (Group) SerializationUtils.clone((Serializable) group));
  }
}