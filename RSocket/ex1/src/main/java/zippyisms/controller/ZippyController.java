package zippyisms.controller;

import zippyisms.utils.Constants;
import zippyisms.datamodel.ZippyQuote;
import zippyisms.datamodel.Subscription;
import zippyisms.datamodel.SubscriptionStatus;
import zippyisms.service.ZippyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * This controller enables RSocket clients to get random Zippy th'
 * Pinhead quotes, subscribe to receive Flux streams of these quotes,
 * as well as cancel earlier subscriptions.  It demonstrates the
 * following RSocket interaction models
 *
 * . Request/Response, where each two-way async request receives a
 * single async response from the server.
 *
 * . Fire-and-Forget, where each one-way message receives no response
 * from the server.
 *
 * . Request/Stream, where each async request receives a stream of
 * responses from the server.
 *
 * . Channel, where a stream of async messages can be sent in both
 * directions between client and server.
 *
 * Spring enables the integration of RSockets into a controller via
 * the @Controller annotation, which enables the autodetection of
 * implementation classes via classpath scanning, and
 * the @MessageMapping annotation, which maps a message onto a
 * message-handling method by matching the declared patterns to a
 * destination extracted from the message.  
 *
 * Combining the @Controller annotation with the @MessageMapping
 * annotation enables this class to declare service endpoints, which
 * in this case map to RSocket endpoints that each take one {@link
 * Mono} or {@link Flux} parameter and can return a {@link Mono} or
 * {@link Flux} result.  The use of {@link Mono} and {@link Flux}
 * types enables client and server code to run reactively across a
 * communication channel.
 */
@Controller
public class ZippyController {
    /**
     * The ZippyService that's associated with this controller via
     * Spring's dependency injection facilities, where an object
     * receives other objects that it depends on (in this case, the
     * ZippyService).
     */
    @Autowired
    private ZippyService mZippyService;

    /**
     * Set of Subscriptions that are used to determine whether a client
     * has subscribed already.
     */
    private final Set<Subscription> mSubscriptions = new HashSet<>();

    /**
     * Subscribe to receive a Flux stream of Zippy quotes.  This
     * method implements a two-way async RSocket request/response call
     * that sends a response back to the client.
     *
     * @param request A {@link Mono} that emits a {@link
     *                Subscription}
     * @return A {@link Mono} that confirms the subscription request.
     */
    @MessageMapping(Constants.SUBSCRIBE)
    Mono<Subscription> subscribe(Mono<Subscription> request) {
        // Return a Mono whose status has been updated to confirm the
        // subscription request.
        return request
            .doOnNext(r -> {
                    // Set the request status to confirm the subscription.
                    r.setStatus(SubscriptionStatus.CONFIRMED);

                    // Add this request to the set of subscriptions.
                    mSubscriptions.add(r);
                })

            // Print the subscription information as a diagnostic.
            .doOnNext(r ->
                      System.out.println("subscribe::"
                                         + r.getRequestId()
                                         + ":"
                                         + r.getStatus()));
    }

    /**
     * Cancel a {@link Subscription} in an unconfirmed manner,
     * i.e., any errors are not indicated to the client.  This method
     * implements a one-way async RSocket fire-and-forget call that
     * does not send a response back to the client.
     *
     * @param request A {@link Mono} that emits a {@link
     *                Subscription}
     */
    @MessageMapping(Constants.CANCEL_UNCONFIRMED)
    void cancelSubscriptionUnconfirmed(Mono<Subscription> request) {
        // Cancel the subscription without informing the client if
        // something goes wrong.
        request
            .doOnNext(r -> {
                    // Print the subscription information as a diagnostic.
                    System.out.print("cancelSubscription::"
                                     + r.getRequestId());

                    // Check whether there's a matching request in the
                    // subscription set.
                    if (mSubscriptions.contains(r)) {
                        // Remove the request from the subscription set.
                        mSubscriptions.remove(r);

                        // Set the request status to indicate the
                        // subscription has been cancelled
                        // successfully.
                        r.setStatus(SubscriptionStatus.CANCELLED);

                        System.out.println(":"
                                               + r.getStatus()
                                               + " cancel succeeded");
                    } else {
                        // Indicate that the subscription wasn't registered.
                        r.setStatus(SubscriptionStatus.ERROR);
                        System.out.println(":"
                                           + r.getStatus()
                                           + " cancel failed");
                    }
                })

            // Initiate the cancellation, which is necessary since no
            // response is sent back to the client.
            .subscribe();
    }

    /**
     * Cancel a {@link Subscription} in a confirmed manner,
     * i.e., any errors are indicated to the client.  This method
     * implements a two-way async RSocket request/response call that
     * sends a response back to the client.
     *
     * @param request A {@link Mono} that emits a {@link
     *                Subscription}
     * @return A {@link Mono} that indicates if the cancel request
     * succeeded or failed.
     */
    @MessageMapping(Constants.CANCEL_CONFIRMED)
    Mono<Subscription> cancelSubscriptionConfirmed(Mono<Subscription> request) {
        // Try to cancel the subscription and indicate if the
        // cancellation succeeded.
        return request
            .map(r -> {
                    // Print the subscription information as a diagnostic.
                    System.out.print("cancelSubscription::"
                                     + r.getRequestId());

                    // Check whether there's a matching request in the
                    // subscription set.
                    if (mSubscriptions.contains(r)) {
                        // Remove the request from the subscription
                        // set.
                        mSubscriptions.remove(r);

                        // Set the request status to indicate the
                        // subscription has been cancelled
                        // successfully.
                        r.setStatus(SubscriptionStatus.CANCELLED);

                        System.out.println(":"
                                           + r.getStatus()
                                           + " cancel succeeded");
                    } else {
                        // Indicate that the subscription wasn't registered.
                        r.setStatus(SubscriptionStatus.ERROR);
                        System.out.println(":"
                                           + r.getStatus()
                                           + " cancel failed");
                    }

                    // Return the updated subscription indicating
                    // success or failure.
                    return r;
                });
    }

    /**
     * Get a {@link Flux} that emits Zippy quotes once a second.  This
     * method implements the async RSocket request/stream model, where
     * each request receives a stream of responses from the server.
     *
     * @param request A {@link Mono} that emits a {@link
     *                Subscription}
     * @return A {@link Flux} that emits Zippy quote every second
     */
    @MessageMapping(Constants.GET_ALL_QUOTES)
    Flux<ZippyQuote> getAllQuotes(Mono<Subscription> request) {
        return request
            .doOnNext(r ->
                          System.out.println("getAllQuotes::"
                                                 + r.getRequestId()
                                                 + ":"
                                                 + r.getStatus()))

            // Check to ensure the subscription request is registered
            // and confirmed.
            .flatMapMany(r -> mSubscriptions
                .contains(r)
                // If the request is subscribed/confirmed
                // return a Flux that emits the list of
                // quotes.
                ? Flux.fromIterable(mZippyService.getQuotes())

                // If the request is not confirmed return an
                // empty Flux.
                : Flux.empty())

            // Delay each emission by one second to demonstrate the
            // streaming capability to clients.
            .delayElements(Duration.ofSeconds(1));
    }

    /**
     * Get a {@link Flux} that emits the requested Zippy quotes.  This
     * method implements a two-way async RSocket bi-directional
     * channel call where a Flux stream is sent to the server and the
     * server returns a Flux in response.
     *
     * @param quoteIds A {@link Flux} that emits the given Zippy
     *                 {@code quoteIds}
     * @return A {@link Flux} that emits the requested Zippy quotes
     */
    @MessageMapping(Constants.GET_RANDOM_QUOTES)
    Flux<ZippyQuote> getRandomQuotes(Flux<Integer> quoteIds) {
        return quoteIds
            // Get the Zippy th' Pinhead quote at each quote id.
            .map(mZippyService::getQuote)

            // Delay each emission by one second to demonstrate the
            // streaming capability to clients.
            .delayElements(Duration.ofSeconds(1));
    }

    /**
     * @return The total number of Zippy th' Pinhead quotes.
     */
    @MessageMapping(Constants.GET_NUMBER_OF_QUOTES)
    Mono<Integer> getNumberOfQuotes() {
        return Mono
            // Return the total number of Zippy th' Pinhead quotes.
            .just(mZippyService.getNumberOfQuotes());
    }
}
