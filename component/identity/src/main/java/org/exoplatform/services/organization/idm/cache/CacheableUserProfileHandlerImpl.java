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

import java.util.Collection;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.cache.OrganizationCacheHandler;
import org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl;
import org.exoplatform.services.organization.idm.PicketLinkIDMService;
import org.exoplatform.services.organization.idm.UserProfileDAOImpl;

public class CacheableUserProfileHandlerImpl extends UserProfileDAOImpl {
  private UserProfile NULL_OBJECT = createUserProfileInstance();

  private final ExoCache<String, UserProfile> userProfileCache;

  @SuppressWarnings("unchecked")
  public CacheableUserProfileHandlerImpl(OrganizationCacheHandler organizationCacheHandler,
                                         PicketLinkIDMOrganizationServiceImpl orgService,
                                         PicketLinkIDMService service) {
    super(orgService, service);
    this.userProfileCache = organizationCacheHandler.getUserProfileCache();
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile findUserProfileByName(String userName) throws Exception {
    UserProfile userProfile = (UserProfile) userProfileCache.get(userName);
    userProfile = userProfile == NULL_OBJECT ? null : userProfile;
    if (userProfile != null)
      return userProfile;

    userProfile = super.findUserProfileByName(userName);
    if (userProfile == null) {
      userProfile = NULL_OBJECT;
    } 
    userProfileCache.put(userName, userProfile);

    return userProfile == NULL_OBJECT ? null : userProfile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UserProfile getProfile(String userName) {
    UserProfile userProfile = (UserProfile) userProfileCache.get(userName);
    if (userProfile != null)
      return userProfile;
  
    userProfile = super.getProfile(userName);
    if (userProfile != null)
      userProfileCache.put(userName, userProfile);
  
    return userProfile;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<UserProfile> findUserProfiles() throws Exception {
    @SuppressWarnings("unchecked")
    Collection<UserProfile> userProfiles = super.findUserProfiles();
    for (UserProfile userProfile : userProfiles)
      userProfileCache.put(userProfile.getUserName(), userProfile);

    return userProfiles;
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile removeUserProfile(String userName, boolean broadcast) throws Exception {
    UserProfile userProfile = super.removeUserProfile(userName, broadcast);
    if (userProfile != null)
      userProfileCache.remove(userName);

    return userProfile;
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserProfile(UserProfile profile, boolean broadcast) throws Exception {
    super.saveUserProfile(profile, broadcast);
    userProfileCache.put(profile.getUserName(), profile);
  }

}
