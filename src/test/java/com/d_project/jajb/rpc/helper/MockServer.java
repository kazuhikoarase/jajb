package com.d_project.jajb.rpc.helper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MockServer
 * @author Kazuhiko Arase
 */
public class MockServer implements InvocationHandler {

  private static final String SERVICES = "/WEB-INF/rpc/service.properties";

  protected static final Logger logger = Logger.getLogger(MockServer.class.getName() );

  private final ServletConfig config;
  private final ServletContext servletContext;
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final HttpServlet target;

  private int statusCode;
  private StringReader in = null;
  private ByteArrayOutputStream out = null;
  private String requestData = "";

  public MockServer(final HttpServlet target) throws Exception {

    config = (ServletConfig)Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class[] { ServletConfig.class }, this);
    servletContext = (ServletContext)Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class[] { ServletContext.class }, this);

    request = (HttpServletRequest)Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class[] { HttpServletRequest.class }, this);
    response = (HttpServletResponse)Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class[] { HttpServletResponse.class }, this);

    target.init(config);

    this.target = target;
  }

  public void doService() throws Exception {
    in = null;
    out = null;
    statusCode = HttpServletResponse.SC_OK;
    logger.finest("reqData: " + requestData);
    target.service(request, response);
    logger.finest("resData: " + getResponseData() );
    if (statusCode != HttpServletResponse.SC_OK) {
      throw new Exception("SC:" + statusCode);
    }
  }

  public void setRequestData(String requestData) {
    this.requestData = requestData;
  }

  public String getResponseData() throws IOException {
    return new String(out.toByteArray(), "UTF-8");
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {

    if (ServletConfig.class.
        isAssignableFrom(method.getDeclaringClass() ) ) {
      if (method.getName().equals("getServletName") ) {
        return "mock";
      } else if (method.getName().equals("getInitParameter") ) {
        if ("services".equals(args[0]) ) {
          return SERVICES;
        } else if ("security-handler".equals(args[0]) ) {
          return null;
        } else if ("application-exceptions".equals(args[0]) ) {
          return null;
        } else {
          throw new Exception( (String)args[0]);
        }
      } else if (method.getName().equals("getServletContext") ) {
          return servletContext;
      } else {
        throw new RuntimeException("not implemented:" + method.getName() );
      }

    } else if (ServletContext.class.
        isAssignableFrom(method.getDeclaringClass() ) ) {
      
      if (method.getName().equals("log") ) {
        return null;
      } else if (method.getName().equals("getResourceAsStream") ) {
        if (!SERVICES.equals(args[0]) ) {
          throw new Exception( (String)args[0]);
        }
        return getClass().getResourceAsStream("/services.properties");
      } else {
        throw new RuntimeException("not implemented:" + method.getName() );
      }

    } else if (ServletRequest.class.
        isAssignableFrom(method.getDeclaringClass() ) ) {
      if (method.getName().equals("getMethod") ) {
        return "POST";
      } else if (method.getName().equals("setCharacterEncoding") ) {
        return null;
      } else if (method.getName().equals("getHeader") ) {
        if ("accept-encoding".equals(args[0]) ) {
          return null;
        } else {
          throw new Exception( (String)args[0]);
        }
      } else if (method.getName().equals("getReader") ) {
        if (in != null) {
          throw new RuntimeException("already called.");
        }
        in = new StringReader(requestData);
        return new BufferedReader(in);
      } else {
        throw new RuntimeException("not implemented:" + method.getName() );
      }

    } else if (ServletResponse.class.
        isAssignableFrom(method.getDeclaringClass() ) ) {
      if (method.getName().equals("setContentType") ) {
        return null;
      } else if (method.getName().equals("setStatus") ) {
        statusCode = (Integer)args[0];
        return null;
      } else if (method.getName().equals("getOutputStream") ) {
        if (out != null) {
          throw new RuntimeException("already called.");
        }
        out = new ByteArrayOutputStream();
        return new ServletOutputStream() {
          @Override
          public void write(int b) throws IOException {
            out.write(b);
          }
        };
      } else if (method.getName().equals("getWriter") ) {
        if (out != null) {
          throw new RuntimeException("already called.");
        }
        out = new ByteArrayOutputStream();
        return new PrintWriter(new OutputStreamWriter(out, "UTF-8") );
      } else {
        throw new RuntimeException("not implemented:" + method.getName() );
      }

    } else {
      return method.invoke(this);
    }
  }
}
