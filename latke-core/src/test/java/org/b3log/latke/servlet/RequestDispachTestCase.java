/*
 * Copyright (c) 2009-2018, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.latke.servlet;

import junit.framework.Assert;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.servlet.handler.AdviceHandler;
import org.b3log.latke.servlet.handler.Handler;
import org.b3log.latke.servlet.handler.MethodInvokeHandler;
import org.b3log.latke.servlet.handler.RouteHandler;
import org.b3log.latke.servlet.mock.TestBeforeAdvice;
import org.b3log.latke.servlet.mock.TestRequestProcessor;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Processor test.
 *
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.4, Dec 2, 2018
 */
public class RequestDispachTestCase {

    private final List<Handler> handlerList = new ArrayList<>();

    static {
        Latkes.init();
    }

    @BeforeTest
    public void beforeTest() {
        final List<Class<?>> classes = new ArrayList<>();
        classes.add(TestRequestProcessor.class);
        classes.add(TestBeforeAdvice.class);
        BeanManager.start(classes);

        final TestRequestProcessor testRequestProcessor = BeanManager.getInstance().getReference(TestRequestProcessor.class);
        DispatcherServlet.get("/l", testRequestProcessor::l);
        DispatcherServlet.mapping();

        handlerList.add(new RouteHandler());
        handlerList.add(new AdviceHandler());
        handlerList.add(new MethodInvokeHandler());
    }

    @AfterTest
    public void afterTest() {
        BeanManager.close();
    }

    //@Test
    public void a() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/a");
        when(request.getMethod()).thenReturn("GET");

        HttpControl control = doFlow(request);
        Assert.assertNotNull(control.data(RouteHandler.MATCH_RESULT));
        Assert.assertNull(control.data(MethodInvokeHandler.INVOKE_RESULT));
    }

    @Test
    public void a1() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/a/88250/D");
        when(request.getMethod()).thenReturn("GET");

        HttpControl control = doFlow(request);
        Assert.assertNotNull(control.data(RouteHandler.MATCH_RESULT));
        Assert.assertNull(control.data(MethodInvokeHandler.INVOKE_RESULT));
    }

    //@Test
    public void l() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/l");
        when(request.getMethod()).thenReturn("GET");

        HttpControl control = doFlow(request);
        Assert.assertNotNull(control.data(RouteHandler.MATCH_RESULT));
        Assert.assertNull(control.data(MethodInvokeHandler.INVOKE_RESULT));
    }

    public HttpControl doFlow(HttpServletRequest req) {
        RequestContext httpRequestContext = new RequestContext();
        httpRequestContext.setRequest(req);
        HttpControl ret = new HttpControl(handlerList.iterator(), httpRequestContext);
        ret.nextHandler();

        return ret;
    }
}