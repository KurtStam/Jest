package io.searchbox.client.config.discovery;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.ClientConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

public class NodeCheckerTest {

    ClientConfig clientConfig;
    JestClient jestClient;

    @Before
    public void setUp() throws Exception {
        clientConfig = new ClientConfig.Builder("http://localhost:9200")
                .discoveryEnabled(true)
                .discoveryFrequency(1l, TimeUnit.SECONDS)
                .build();

        jestClient = mock(JestClient.class);
    }

    @Test
    public void testWithResolvedWithoutHostnameAddressWithCustomScheme() throws Exception {
        NodeChecker nodeChecker = new NodeChecker(jestClient, new ClientConfig.Builder("http://localhost:9200")
                .discoveryEnabled(true)
                .discoveryFrequency(1l, TimeUnit.SECONDS)
                .defaultSchemeForDiscoveredNodes("https")
                .build());

        JestResult result = new JestResult(new Gson());
        result.setJsonMap(ImmutableMap.<String, Object>of(
                "ok", "true",
                "nodes", ImmutableMap.of(
                        "node_name", ImmutableMap.of(
                                "http_address", "inet[/192.168.2.7:9200]"
                        )
                )
        ));
        result.setSucceeded(true);
        when(jestClient.execute(isA(Action.class))).thenReturn(result);

        nodeChecker.runOneIteration();

        verify(jestClient).execute(isA(Action.class));
        ArgumentCaptor<LinkedHashSet> argument = ArgumentCaptor.forClass(LinkedHashSet.class);
        verify(jestClient).setServers(argument.capture());
        verifyNoMoreInteractions(jestClient);

        assertTrue(argument.getValue().contains("https://192.168.2.7:9200"));
    }

    @Test
    public void testWithResolvedWithoutHostnameAddress() throws Exception {
        NodeChecker nodeChecker = new NodeChecker(jestClient, clientConfig);

        JestResult result = new JestResult(new Gson());
        result.setJsonMap(ImmutableMap.<String, Object>of(
                "ok", "true",
                "nodes", ImmutableMap.of(
                        "node_name", ImmutableMap.of(
                                "http_address", "inet[/192.168.2.7:9200]"
                        )
                )
        ));
        result.setSucceeded(true);
        when(jestClient.execute(isA(Action.class))).thenReturn(result);

        nodeChecker.runOneIteration();

        verify(jestClient).execute(isA(Action.class));
        ArgumentCaptor<LinkedHashSet> argument = ArgumentCaptor.forClass(LinkedHashSet.class);
        verify(jestClient).setServers(argument.capture());
        verifyNoMoreInteractions(jestClient);

        assertTrue(argument.getValue().contains("http://192.168.2.7:9200"));
    }

    @Test
    public void testWithResolvedWithHostnameAddress() throws Exception {
        NodeChecker nodeChecker = new NodeChecker(jestClient, clientConfig);

        JestResult result = new JestResult(new Gson());
        result.setJsonMap(ImmutableMap.<String, Object>of(
                "ok", "true",
                "nodes", ImmutableMap.of(
                        "node_name", ImmutableMap.of(
                                "http_address", "inet[searchly.com/192.168.2.7:9200]"
                        )
                )
        ));
        result.setSucceeded(true);
        when(jestClient.execute(isA(Action.class))).thenReturn(result);

        nodeChecker.runOneIteration();

        verify(jestClient).execute(isA(Action.class));
        ArgumentCaptor<LinkedHashSet> argument = ArgumentCaptor.forClass(LinkedHashSet.class);
        verify(jestClient).setServers(argument.capture());
        verifyNoMoreInteractions(jestClient);

        assertTrue(argument.getValue().contains("http://192.168.2.7:9200"));
    }

    @Test
    public void testWithUnresolvedAddress() throws Exception {
        NodeChecker nodeChecker = new NodeChecker(jestClient, clientConfig);

        JestResult result = new JestResult(new Gson());
        result.setJsonMap(ImmutableMap.<String, Object>of(
                "ok", "true",
                "nodes", ImmutableMap.of(
                        "node_name", ImmutableMap.of(
                                "http_address", "inet[192.168.2.7:9200]"
                        )
                )
        ));
        result.setSucceeded(true);
        when(jestClient.execute(isA(Action.class))).thenReturn(result);

        nodeChecker.runOneIteration();

        verify(jestClient).execute(isA(Action.class));
        ArgumentCaptor<LinkedHashSet> argument = ArgumentCaptor.forClass(LinkedHashSet.class);
        verify(jestClient).setServers(argument.capture());
        verifyNoMoreInteractions(jestClient);

        assertTrue(argument.getValue().contains("http://192.168.2.7:9200"));
    }

    @Test
    public void testWithInvalidUnresolvedAddress() throws Exception {
        NodeChecker nodeChecker = new NodeChecker(jestClient, clientConfig);

        JestResult result = new JestResult(new Gson());
        result.setJsonMap(ImmutableMap.<String, Object>of(
                "ok", "true",
                "nodes", ImmutableMap.of(
                        "node_name", ImmutableMap.of(
                                "http_address", "inet[192.168.2.7:]"
                        )
                )
        ));
        result.setSucceeded(true);
        when(jestClient.execute(isA(Action.class))).thenReturn(result);

        nodeChecker.runOneIteration();

        verify(jestClient).execute(isA(Action.class));
        ArgumentCaptor<LinkedHashSet> argument = ArgumentCaptor.forClass(LinkedHashSet.class);
        verify(jestClient).setServers(argument.capture());
        verifyNoMoreInteractions(jestClient);

        assertTrue("Should be empty: " + argument.getValue(), argument.getValue().isEmpty());
    }

    @Test
    public void testWithInvalidResolvedAddress() throws Exception {
        NodeChecker nodeChecker = new NodeChecker(jestClient, clientConfig);

        JestResult result = new JestResult(new Gson());
        result.setJsonMap(ImmutableMap.<String, Object>of(
                "ok", "true",
                "nodes", ImmutableMap.of(
                        "node_name", ImmutableMap.of(
                                "http_address", "inet[gg/192.168.2.7:]"
                        )
                )
        ));
        result.setSucceeded(true);
        when(jestClient.execute(isA(Action.class))).thenReturn(result);

        nodeChecker.runOneIteration();

        verify(jestClient).execute(isA(Action.class));
        ArgumentCaptor<LinkedHashSet> argument = ArgumentCaptor.forClass(LinkedHashSet.class);
        verify(jestClient).setServers(argument.capture());
        verifyNoMoreInteractions(jestClient);

        assertTrue("Should be empty: " + argument.getValue(), argument.getValue().isEmpty());
    }

}
