/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
import java.util.List;

import org.apache.commons.lang.SerializationUtils;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.cache.MembershipCacheKey;
import org.exoplatform.services.organization.cache.OrganizationCacheHandler;
import org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl;
import org.exoplatform.services.organization.idm.PicketLinkIDMService;
import org.exoplatform.services.organization.idm.UserDAOImpl;

public class CacheableUserHandlerImpl extends UserDAOImpl
{

   private final ExoCache<String, User> userCache;

   private final ExoCache<String, UserProfile> userProfileCache;

   private final ExoCache<Serializable, Membership> membershipCache;

   @SuppressWarnings("unchecked")
  public CacheableUserHandlerImpl(OrganizationCacheHandler organizationCacheHandler, PicketLinkIDMOrganizationServiceImpl orgService, PicketLinkIDMService idmService) {
      super(orgService, idmService);
      this.userCache = organizationCacheHandler.getUserCache();
      this.userProfileCache = organizationCacheHandler.getUserProfileCache();
      this.membershipCache = organizationCacheHandler.getMembershipCache();
   }

   /**
    * {@inheritDoc}
    */
   public User removeUser(String userName, boolean broadcast) throws Exception
   {
      User user = super.removeUser(userName, broadcast);
      if (user != null)
      {
         userCache.remove(userName);
         userProfileCache.remove(userName);

         List<? extends Membership> memberships = membershipCache.getCachedObjects();
         for (Membership membership : memberships)
         {
            if (membership.getUserName().equals(userName))
            {
               membershipCache.remove(membership.getId());
               membershipCache.remove(new MembershipCacheKey(membership));
            }
         }
      }

      return user;
   }

   /**
    * {@inheritDoc}
    */
   public void saveUser(User user, boolean broadcast) throws Exception
   {
      super.saveUser(user, broadcast);
      userCache.put(user.getUserName(), user);
   }

   /**
    * {@inheritDoc}
    */
   public void createUser(User user, boolean broadcast) throws Exception
   {
     super.createUser(user, broadcast);
     userCache.put(user.getUserName(), user);
   }

   /**
    * {@inheritDoc}
    */
   public User setEnabled(String userName, boolean enabled, boolean broadcast) throws Exception
   {
      User result = super.setEnabled(userName, enabled, broadcast);
      if (result != null)
      {
         userCache.put(result.getUserName(), result);
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public User findUserByName(String userName, UserStatus status) throws Exception
   {
      User user = userCache.get(userName);
      if (user == null) {
        user = super.findUserByName(userName, status);
        if (user != null)
           userCache.put(userName, user);
      }

      return user == null ? null : (status.matches(user.isEnabled()) ? (User) SerializationUtils.clone((Serializable) user) : null);
   }
}
