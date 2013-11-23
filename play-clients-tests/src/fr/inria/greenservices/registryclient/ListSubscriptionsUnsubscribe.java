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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ow2.play.governance.api.GovernanceExeption;
import org.ow2.play.governance.api.SubscriptionRegistry;
import org.ow2.play.governance.api.bean.Subscription;
import org.ow2.play.governance.api.bean.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Iyad Alshabani
 *
 */
public class ListSubscriptionsUnsubscribe {
	private final static Logger logger = LoggerFactory
			.getLogger(ListSubscriptionsUnsubscribe.class);
	private static String playServiceEndpoint = "http://138.96.19.115:8080/play/registry/RegistryService";

	private static String topicName = "MyTopic";

	private static SubscriptionRegistry sreg;
	private static ExtendedPlayClient cPlay;

	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.err.println("Usage program  TopicName PlayEndpoint");
			System.err
					.println("PlayEndpoint is where the PLAY service is running, if not give the default one on play.inria.fr is used");
			System.exit(-1);
		} else {
			topicName = args[0];
			
			if (args.length ==2) playServiceEndpoint = args[1];

		}

		Topic topic = new Topic();
		topic.setName(topicName);
		topic.setNs("http://streams.event-processing.org/ids/");
		topic.setPrefix("s");
		cPlay = new ExtendedPlayClient(playServiceEndpoint);
		System.out.println("Got a client from playServiceEndpoint: "
				+ playServiceEndpoint);

		sreg = cPlay.getSubscriptionRegistryService();

		//get the list of subscription for a topic
		List<Subscription> subList = getSubscriptions(topic);
		//remove these subscriptions
		removeSubscriptions(subList);

	}

	// get all subscriptions for a given topic
	// @SuppressWarnings("null")
	@SuppressWarnings("null")
	public static List<Subscription> getSubscriptions(Topic topic)
			throws Exception {
		List<Subscription> subList;
		List<Subscription> retList = new ArrayList<>();
		;
		int count = 0;

		try {
			subList = sreg.getSubscriptions();
			System.out.println("=========Number of subscription there : "
					+ subList.size() + "      ========");
			for (Iterator<Subscription> iter = subList.iterator(); iter
					.hasNext();) {

				Subscription sss = iter.next();
				// System.out.println("Subscription ====  "+sss.toString());
				if (sss.getTopic().equals(topic)) {
					System.out.println("### " + count + " ###"
							+ sss.getTopic().getName());
					retList.add(sss);
					System.out.println("Subscription in the list "
							+ sss.toString());
					count++;

				}
			}

		} catch (GovernanceExeption e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retList;
	}

	// unSubscribe a List of subscription

	public static boolean removeSubscriptions(
			List<Subscription> subscriptionList) {

		// we can try to use the subscription service to unsubscribe
		// SubscriptionService subServ = cPlay.getSubscriptionService();

		if (subscriptionList == null) {
			logger.info("NO Subscription List");
			return true;
		}

		int count = 0;

		for (Iterator<Subscription> iter = subscriptionList.iterator(); iter
				.hasNext();) {
			Subscription tt = iter.next();
			logger.info("-- Subscriber -- {}" + tt);
			try {
				// if using the unsubscribe(Subscription, Provider)
				// unsubscribe(tt,tt.getProvider());
				sreg.remove(tt);
			} catch (GovernanceExeption e) {
				// TODO Auto-generated catch block
				System.out.println("!!!!!!!!!!Removal Problem  : " + tt);
				// for un subscription
				// System.out.println("!!!!!!!!!!Unsubscription Problem  : "+tt
				// );
				System.out.println(e.getMessage());
			}
			count++;

		}

		logger.info("{} Subscriptions removed " + count);
		// FIXME return value

		return true;
	}

}
