package zippyisms.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import zippyisms.client.ZippyMicroserviceClient;
import zippyisms.datamodel.Subscription;
import zippyisms.datamodel.SubscriptionStatus;
import zippyisms.datamodel.ZippyQuote;

import java.util.UUID;

/**
 * This class tests the endpoints provided by the ZippyApplication
 * microservice for each of the four interaction models supported by
 * RSocket.  The @SpringBootTest annotation tells Spring to look for a
 * main configuration class (e.g., one with @SpringBootApplication)
 * and use that to start a Spring application context.  The 
 * @ComponentScan annotation enables auto-detection of beans by a
 * Spring container.  Java classes that are decorated with stereotypes
 * such as @Component, @Configuration, @Service are auto-detected by
 * Spring.
 */
@SpringBootTest
@ComponentScan(basePackageClasses = ZippyMicroserviceClient.class)
public class ZippyMicroserviceTest {
    /**
     * The number of Zippy th' Pinhead quotes to process.
     */
    private static final int sNUMBER_OF_INDICES = 5;

    /**
     * This object connects to the ZippyMicroserviceClient.  The
     * @Autowired annotation marks this field to be initialized via
     * Spring's dependency injection facilities, where an object
     * receives other objects that it depends on (in this case, by
     * creating a ZippyMicroserviceClient).
     */
    @Autowired
    private ZippyMicroserviceClient zippyClient;

    /**
     * Get/print/test that specified number of random Zippy th'
     * Pinhead quotes are received.  This method demonstrates a
     * two-way RSocket bi-directional channel call where a Flux stream
     * is sent to the server and the server returns a Flux in
     * response.
     */
    @Test
    public void testGetRandomQuotes() {
        System.out.println("Entering testGetRandomQuotes()");

        Mono<Integer[]> randomIndices = zippyClient
            // Make random indices needed for the test.
            .makeRandomIndices(sNUMBER_OF_INDICES)

            // Turn this Mono into a hot source and cache last emitted
            // signals for further subscribers.
            .cache();

        Flux<ZippyQuote> zippyQuotes = zippyClient
            // Create a Flux that emits Zippy th' Pinhead quotes at the
            // random indices emitted by the randomZippyQuotes Flux.
            .getRandomQuotes(randomIndices)

            // Print the Zippyisms emitted by the Flux<ZippyQuote>.
            .doOnNext(m -> System.out.println("Quote ("
                                              + m.getQuoteId() + ") = "
                                              + m.getZippyism()));

        // Make a copy of the array so the test below will work properly.
        Integer[] ri = randomIndices.block();

        assert ri != null;

        // Ensure the results are correct, i.e., the returned quoteIds
        // match those sent to the GET_QUOTE endpoint.
        StepVerifier.create(zippyQuotes)
            .expectNextMatches(m -> m.getQuoteId() == ri[0])
            .expectNextMatches(m -> m.getQuoteId() == ri[1])
            .expectNextMatches(m -> m.getQuoteId() == ri[2])
            .expectNextMatches(m -> m.getQuoteId() == ri[3])
            .expectNextMatches(m -> m.getQuoteId() == ri[4])
            .verifyComplete();
    }

    /**
     * Subscribe and cancel requests to receive Zippyisms.  This
     * method demonstrates a two-way async RSocket request/response
     * call that subscribes to retrieve a stream of Zippy t' Pinhead
     * quotes.
     */
    @Test
    public void testSubscribeAndCancel() {
        System.out.println("Entering testSubscribeAndCancel()");

        // Create a Mono<SubscriptionRequest>.
        Mono<Subscription> subscriptionRequest = zippyClient
            // Subscribe using a random ID.
            .subscribe(UUID.randomUUID())

            // Print the results as a diagnostic.
            .doOnNext(r ->
                      System.out.println(r.getRequestId()
                                         + ":" + r.getStatus()));

        // Ensure that the subscriptionRequest's status is CONFIRMED.
        StepVerifier
            .create(subscriptionRequest)
            .expectNextMatches(r -> r
                               .getStatus()
                               .equals(SubscriptionStatus.CONFIRMED))
            .verifyComplete();

        Mono<Subscription> mono = zippyClient
            // Perform a confirmed cancellation of the subscription
            // (should succeed).
            .cancelConfirmed(subscriptionRequest);

        // Test that the subscription was successfully cancelled.
        StepVerifier
            .create(mono)
            .expectNextMatches(r -> r
                               .getStatus()
                               .equals(SubscriptionStatus.CANCELLED))
            .verifyComplete();

        mono = zippyClient
            // Try to cancel the subscription (which intentionally fails
            // since there was no registered subscription with this ID).
            .cancelConfirmed(UUID.randomUUID());

        // Test that the subscription was unsuccessfully cancelled.
        StepVerifier
            .create(mono)
            .expectNextMatches(r -> r
                               .getStatus()
                               .equals(SubscriptionStatus.ERROR))
            .verifyComplete();
    }

    /**
     * Subscribe for and receive sNUMBER_OF_QUOTES of Zippy th'
     * Pinhead quotes.  This method demonstrates the async RSocket
     * request/stream model, where each request receives a Flux stream
     * of responses from the server.
     */
    @Test
    public void testValidSubscribeForQuotes() {
        System.out.println("Entering testValidSubscribeForQuotes()");

        Mono<Subscription> subscriptionRequest = zippyClient
            // Get a confirmed SubscriptionRequest from the server.
            .subscribe(UUID.randomUUID());

        Flux<ZippyQuote> zippyQuotes = zippyClient
            // Use the confirmed SubscriptionRequest to get a Flux that
            // emits ZippyQuote objects from the server.
            .getAllQuotes(subscriptionRequest)

            // Print each Zippyism emitted by the Flux<ZippyQuote>.
            .doOnNext(m ->
                      System.out.println("Quote: " + m.getZippyism()))

            // Only emit sNUMBER_OF_QUOTES.
            .take(sNUMBER_OF_INDICES);

        // Ensure the first five results come in the right order.
        StepVerifier.create(zippyQuotes)
            .expectNextMatches(m -> m
                               .getZippyism()
                               .equals("All of life is a blur of Republicans and meat!"))
            .expectNextMatches(m -> m
                               .getZippyism()
                               .equals("..Are we having FUN yet...?"))
            .expectNextMatches(m -> m
                               .getZippyism()
                               .equals("Life is a POPULARITY CONTEST!  I'm REFRESHINGLY CANDID!!"))
            .expectNextMatches(m -> m
                               .getZippyism()
                               .equals("You were s'posed to laugh!"))
            .expectNextMatches(m -> m
                               .getZippyism()
                               .equals("Fold, fold, FOLD!!  FOLDING many items!!"))
            .verifyComplete();
    }

    /**
     * Try to subscribe for and receive sNUMBER_OF_QUOTES of Zippy th'
     * Pinhead quotes, which fails because the {@link Subscription}
     * has been cancelled.  It also demonstrates a one-way RSocket
     * fire-and-forget call that does not return a response.
     */
    @Test
    public void testInvalidSubscribeForQuotes() {
        System.out.println("Entering testInvalidSubscribeForQuotes()");

        // Get a confirmed SubscriptionRequest from the server.
        Mono<Subscription> subscriptionRequest = zippyClient
            // Subscribe using a random ID.
            .subscribe(UUID.randomUUID())

            // Print the results as a diagnostic.
            .doOnNext(r ->
                      System.out.println("subscribe-returned::"
                                         + r.getRequestId()
                                         + ":" + r.getStatus()));

        // Ensure that the subscriptionRequest's status is CONFIRMED.
        StepVerifier
            .create(subscriptionRequest)
            .expectNextMatches(r -> r
                               .getStatus()
                               .equals(SubscriptionStatus.CONFIRMED))
            .verifyComplete();

        Mono<Void> mono = zippyClient
            // Perform a one-way unconfirmed cancellation of
            // subscriptionRequest.
            .cancelUnconfirmed(subscriptionRequest);

        // Test that the mono completes, which is the best we can do
        // since s no useful value is returned from a one-way message.
        StepVerifier
            .create(mono)
            .verifyComplete();

        Flux<ZippyQuote> zippyQuotes = zippyClient
            // Attempt to get all the Zippy th' Pinhead quotes, which
            // fails since the subscriptionRequest was cancelled.
            .getAllQuotes(subscriptionRequest);

        // Ensure the Flux completes with an error exception since we
        // passed a cancelled Subscription.
        StepVerifier.create(zippyQuotes)
            .expectError(IllegalAccessException.class)
            .verify();
    }
}
