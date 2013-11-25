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

package fr.inria.greenservices.playsubscriber;

/**
 * @author Iyad Alshabani
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.ow2.play.governance.api.GovernanceExeption;
import org.ow2.play.governance.api.bean.Subscription;
import org.ow2.play.governance.api.bean.Topic;
import org.ow2.play.service.client.ClientException;
import org.ow2.play.service.client.PlayClient;
import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.notification.service.NotificationConsumerService;
import org.petalslink.dsb.soap.CXFExposer;
import org.petalslink.dsb.soap.api.Exposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.NotificationMessageHolderType;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.utils.RDFReader;

//import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
//import eu.play_project.play_eventadapter.AbstractReceiver;
import eu.play_project.play_commons.eventformat.EventFormatHelpers;

// TODO: Auto-generated Javadoc
/**
 * The Class GreenPlayClient.
 */
public class GreenPlayClient {
	private final static Logger logger = LoggerFactory
			.getLogger(GreenPlayClient.class);
	// private String SubscriberEndPoint = null;

	/** The Constant producer. */
	// private static final String producer = null;

	/** The started. */
	protected static boolean started = false;

	// private CXFExposer exposer;
	/** The server. */
	private Service server = null;

	/** The new sub id. */
	private String subscriptionId;

	protected String userId;

	protected String pollutionRate;

	protected String polluant;

	protected static String mode = "mail";

	private static String dsbSubscribeTo = "http://play.inria.fr:8084/petals/services/NotificationProducerPortService";

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	// private static final int waitForResults = 5 * 60;

	// private QName topic;

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String[] args) throws Exception {

		// FIXME : replace by the platform registry endpoint
		// String endpoint = "http://localhost:8080/registry/RegistryService";
		// String endpoint =
		// "http://46.105.181.221:8080/registry/RegistryService";

		String subscriberEndpoint = "http://localhost:8199/pubsubmgs/service";
		if (args.length < 1) {
			System.err.println("Usage GreenPlayClient Endpoint <MODE>");
			System.err
					.println("MODE= mail|sms, if not given  the defail valu is mail");
			System.exit(-1);
		} else {
			subscriberEndpoint = args[0];
			if (args.length == 2)
				mode = args[1];

		}

		String playServiceEndpoint = "http://138.96.19.115:8080/play/registry/RegistryService";

		Topic topic = new Topic();
		topic.setName("PollutionAlert");
		topic.setNs("http://streams.event-processing.org/ids/");
		topic.setPrefix("s");
		// add shutdown hook to gracefully catch CTRL-C

		final PlayClient client = new PlayClient(playServiceEndpoint);
		System.out.println("Got a client from playServiceEndpoint: "
				+ playServiceEndpoint);
		// System.out.println("client registry "+client.);
		/*String dsbSubscribeTo = client.getEventGovernance()
				.createSubscriberTopic(topic);

		System.out.println("You can subscribe to : " + dsbSubscribeTo);
*/
		// we can now subscribe to the DSB topic. The WSN subscribe endpoint has
		// been returned by the governance operation call.
		// You can use the standard WSN subscribe API or use the governance one
		// like below:

		// Subscriber endpoint ie the service which subscribe to receive
		// notifications

		final AtomicLong counter = new AtomicLong(0);

		GreenPlayClient gpc = new GreenPlayClient();
		// gpc.SubscriberEndPoint=
		// "http://greenservices.inria.fr:8199/pubsubmgs/service";
		gpc.server = gpc.startServer(subscriberEndpoint, counter);
		Subscription subscription = new Subscription();
		subscription.setTopic(topic);
		subscription.setProvider(dsbSubscribeTo );
		subscription.setSubscriber(subscriberEndpoint);
		final Subscription result = client.getSubscriptionService().subscribe(
				subscription);

		System.out.println("Subscribed : " + result);
		gpc.setSubscriptionId(result.getId());

		// you will now receive notification on your endpoint when they areah
		// published to the EC by the CEP.

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutting down...");
				System.out.println("Try to unsubscribe from " + result.getId()
						+ " from this provide" + result.getProvider());
				try {
					if (client.getSubscriptionService().unsubscribe(result,
							result.getProvider())) {
						System.out.println("Suceeded to unsubscribed");

					} else
						System.out
								.println("********************* unsubscribe Failed ");
				} catch (GovernanceExeption e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				synchronized (GreenPlayClient.class) {
					started = false;
					System.out.println("to notify main thread....");
					GreenPlayClient.class.notifyAll();
				}
			}
		});
		synchronized (GreenPlayClient.class) {
			try {
				System.out.println("before wait ...");
				GreenPlayClient.class.wait();
				gpc.server.stop();
				System.out.println("Stopped!");
				System.out.println("after wait ...");
			} catch (InterruptedException e) {
				System.out.println("Got InterruptedException.");
			}
		}

		GreenPlayClient.class.wait();
	}

	/**
	 * Start server.
	 * 
	 * @param subscriberEndPointAddress
	 *            the subscriber end point address on which the service run
	 * @param counter
	 *            the counter of how many notifications received
	 * @return the service object
	 */
	private Service startServer(final String subscriberEndPointAddress,
			final AtomicLong counter) {
		System.out.println("****** CREATING LOCAL SERVER ******");

		// local address which will receive notifications
		System.out
				.println("Creating service which will receive notification messages from the DSB...");

		QName interfaceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
				"NotificationConsumer");
		QName serviceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
				"NotificationConsumerService");
		QName endpointName = new QName("http://docs.oasis-open.org/wsn/bw-2",
				"NotificationConsumerPort");
		// expose the service
		INotificationConsumer consumer = new INotificationConsumer() {
			// private final AbstractReceiver rdfParser = new AbstractReceiver()
			// {
			// };

			public void notify(Notify notify) throws WsnbException {

				NotificationMessageHolderType notificationMessage = notify
						.getNotificationMessage().get(0);

				InputStream is = new ByteArrayInputStream(EventFormatHelpers
						.unwrapFromDomNativeMessageElement(
								(Element) notificationMessage.getMessage()
										.getAny()).getBytes());

				/*
				 * FIXME to be fixed by GrreenServices server to be able to use
				 * the content of the event
				 * 
				 * Iterating on the event and get the values it needs
				 */

				CompoundEvent initialEvent = new CompoundEvent(RDFReader.read(
						is, SerializationFormat.TriG));
				getValuesFromQuadruple(initialEvent, "idUser");
				getValuesFromQuadruple(initialEvent, "pollutionRate");
				getValuesFromQuadruple(initialEvent, "polluant");

				logger.info("Initial quadruples are:");
				logInfo(initialEvent);
				// logger.info("Hashmap is used and the map <predicate, value> is ",
				// getPredicateValuesMap().toString());

				userId = getPredicateValuesMap().get("idUser");

				System.out
						.println("***\n \n ***\n Got a PollutionAlert to the user *************** "
								+ "******************************************"
								+ "******************************************: {} --- {} --- {}"
								+ userId);
				// );

				GreenservicesPHPAdapter.sendToMGS(userId, mode);

				System.out
						.println(String
								.format("Got a notify on HTTP service #%s, this notification comes from the DSB itself...",
										counter.incrementAndGet()));

				Document dom = Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(
						notify);

				System.out.println("==============================");
				try {
					XMLHelper.writeDocument(dom, System.out);
				} catch (TransformerException e) {
				}
				System.out.println("==============================");

			}
		};
		NotificationConsumerService service = new NotificationConsumerService(
				interfaceName, serviceName, endpointName,
				"NotificationConsumerService.wsdl", subscriberEndPointAddress,
				consumer);

		Exposer exposer = new CXFExposer();
		try {
			server = exposer.expose(service);
			server.start();
			System.out
					.println("Local server is started and is ready to receive notifications");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return server;

	}

	private static void logInfo(CompoundEvent event) {
		for (Quadruple quad : event) {
			logger.info(quad.toString());
		}
	}

	// add the value and it's key into a map for this Event
	private void getValuesFromQuadruple(CompoundEvent quads, String key) {
		HashMap<String, String> predval = this.getPredicateValuesMap();
		for (Quadruple quadruple : quads) {
			if (quadruple.getPredicate().getURI().toString().contains(key)) {
				if (quadruple.getObject().isLiteral()) {
					predval.put(key, quadruple.getObject().getLiteral()
							.toString());
					System.out.println("getting ---"
							+ quadruple.getObject().getLiteral().toString());
				}
				if (quadruple.getObject().isURI()) {
					predval.put(key, quadruple.getObject().getURI().toString());
					System.out.println("getting --- "
							+ quadruple.getObject().getURI().toString());
				}

			}
		}
		// set a new hash map for the current event,
		// for the next notification the hash map is reset
		setPredicateValuesMap(predval);
	}

	private HashMap<String, String> predicateValuesMap = new HashMap<String, String>();

	public HashMap<String, String> getPredicateValuesMap() {
		return predicateValuesMap;
	}

	public void setPredicateValuesMap(HashMap<String, String> predicateValuesMap) {
		this.predicateValuesMap = predicateValuesMap;
	}

}
