/**
 * Copyright (c) 2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/

package fr.inria.greenservices.registryclient;

import org.ow2.play.governance.api.SubscriptionManagement;
import org.ow2.play.governance.api.SubscriptionRegistry;
import org.ow2.play.service.client.ClientException;
import org.ow2.play.service.client.PlayClient;
import org.ow2.play.service.registry.api.Constants;


/**
 * @author Iyad Alshabani
 *
 */
public class ExtendedPlayClient extends PlayClient {

	public ExtendedPlayClient(String registryEndpoint) {
		super(registryEndpoint);
		// TODO Auto-generated constructor stub
	}
	
	public SubscriptionManagement getSubscriptionManagementService () throws ClientException {
		return getWSClient(Constants.GOVERNANCE_SUBSCRIPTIONMANAGEMENT_SERVICE, SubscriptionManagement.class);
	}
	public SubscriptionRegistry getSubscriptionRegistryService () throws ClientException {
		return getWSClient(Constants.GOVERNANCE_SUBSCRIPTION_REGISTRY, SubscriptionRegistry.class );
	}
	
}
