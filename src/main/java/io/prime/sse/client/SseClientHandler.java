package io.prime.sse.client;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krungsri.auth.mobile.gateway.event.SseRecievedEvent;
import com.krungsri.auth.mobile.shared.event.manager.EventManager;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;

public class SseClientHandler extends SimpleChannelInboundHandler<HttpObject>
{
	private static final Pattern DISPATCH_PATTERN = Pattern.compile("^(\r\n|\r|\n)+");
	private static final Pattern DIGITS_ONLY = Pattern.compile("^[\\d]+$");
	
	private static final String DATA = "data";
    private static final String ID = "id";
    private static final String EVENT = "event";
    private static final String RETRY = "retry";
    private static final String DEFAULT_EVENT = "message";
    private static final String EMPTY_STRING = "";
    
    private SseMessage sseMessage;
    {
    	createNewMessage();
    }
    private void createNewMessage(){
    	sseMessage = new SseMessage();
    	sseMessage.setEvent(DEFAULT_EVENT);
    }
	
	private Logger logger = LoggerFactory.getLogger(SseClientHandler.class);
	
	private String line = "";
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception 
	{
		if ( msg instanceof HttpResponse ) {
			msg.decoderResult().isSuccess();
		}
		if ( msg instanceof HttpContent ) {
			String content = ((HttpContent) msg).retain().content().toString(Charset.forName("UTF-8"));
			if ( DISPATCH_PATTERN.matcher(content).matches() ) {
				String[] splittedLine = line.split("(\r\n|\r|\n)", 0);
				for (int i = 0; i < splittedLine.length; i++) {
					String toProcessLine = splittedLine[i];
					this.processLine(toProcessLine);
				}
				this.processLine(EMPTY_STRING);
				this.line = "";
			} else {
				this.line += content;
			}
		}
	}
	
	public void processLine(String line) {
        int colonIndex;
         if (line.trim().isEmpty()) {
        	 this.dispatch(this.sseMessage);
        	 this.createNewMessage();
        } else if (line.startsWith(":")) {
            // ignore
        } else if ((colonIndex = line.indexOf(":")) != -1) {
            String field = line.substring(0, colonIndex);
            String value = line.substring(colonIndex + 1).replaceFirst(" ", EMPTY_STRING);
            processField(field, value);
        } else {
            processField(line.trim(), EMPTY_STRING); // The spec doesn't say we need to trim the line, but I assume that's an oversight.
        }
    }
	
	private void dispatch(SseMessage message)
	{
		try {
			SseRecievedEvent event = new SseRecievedEvent();
			event.setMessage(message);
			EventManager events = EventManager.INSTANCE;
			events.notifyObservers(event);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void processField(String field, String value) {
        if (DATA.equals(field)) {
        	sseMessage.getData().add(value);
        } else if (ID.equals(field)) {
        	sseMessage.setId(value);
        } else if (EVENT.equals(field)) {
            sseMessage.setEvent(value);
        } else if (RETRY.equals(field) && isNumber(value)) {
        	sseMessage.setRetry(value);
        }
    }
	
	private boolean isNumber(String value) {
        return DIGITS_ONLY.matcher(value).matches();
    }
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info("##### SSE CLIENT CHANNEL INACTIVE");
	}

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.info("##### SSE CLIENT EXCEPTION CAUGHT", cause);
    }

	
	
}
