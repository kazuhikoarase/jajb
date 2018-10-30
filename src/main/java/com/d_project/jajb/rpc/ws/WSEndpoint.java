package com.d_project.jajb.rpc.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

/**
 * WSEndpoint
 * @author Kazuhiko Arase
 */
public class WSEndpoint extends Endpoint {

  private static final ConcurrentHashMap<String,Object> global;
  protected static final Logger logger;

  static {
    global = new ConcurrentHashMap<String,Object>();
    global.put("contextMap", new ConcurrentHashMap<String,Context>() );
    logger = Logger.getLogger(WSEndpoint.class.getName() );
  }

  public WSEndpoint() {
  }

  protected ConcurrentHashMap<String,Object> getGlobal() {
    return global;
  }

  @SuppressWarnings("unchecked")
  protected ConcurrentHashMap<String,Context> getContextMap() {
    return (ConcurrentHashMap<String,Context>)global.get("contextMap");
  }

  protected IEndpoint createEndpoint(
      final Session session, final EndpointConfig config) {
    try {

      final ServletContext servletContext = (ServletContext)config.
          getUserProperties().get("servletContext");
      final String factory = (String)config.
          getUserProperties().get("factory");

      final Map<String,Object> endpointConfig = new HashMap<String,Object>();
      endpointConfig.put("$global", global);
      endpointConfig.put("$logger", logger);
      endpointConfig.put("$session", session);
      endpointConfig.put("$servletContext", servletContext);
      endpointConfig.put("$request", config.getUserProperties().get("request") );

      // clear properties.
      config.getUserProperties().clear();

      return ((IEndpointContext)Class.
          forName(factory).newInstance()).createEndpoint(endpointConfig);

    } catch(RuntimeException e) {
      throw e;
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onOpen(Session session, EndpointConfig config) {
    final Context context = new Context(session,
        createEndpoint(session, config) );
    session.addMessageHandler(new MessageHandler.Whole<String>() {
      @Override
      public void onMessage(String message) {
        context.getEndpoint().onMessage(message);
      }
    });
    getContextMap().put(session.getId(), context);
    context.getEndpoint().onOpen(config);
  }

  @Override
  public void onClose(Session session, CloseReason closeReason) {
    Context context = getContextMap().get(session.getId() );
    context.getEndpoint().onClose(closeReason);
    getContextMap().remove(session.getId() );
  }

  @Override
  public void onError(Session session, Throwable t) {
    if (t instanceof IOException) {
      // ignore
    } else {
      logger.log(Level.SEVERE, t.getMessage(), t);
    }
  }
}
