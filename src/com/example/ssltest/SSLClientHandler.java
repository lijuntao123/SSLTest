package com.example.ssltest;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;


public class SSLClientHandler implements IoHandler {

	@Override
	public void exceptionCaught(IoSession session, Throwable msg)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageReceived(IoSession session, Object msg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("from addr is :"+session.getRemoteAddress());
		System.out.println("msg is :"+msg);

	}

	@Override
	public void messageSent(IoSession session, Object msg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("sent msg is :"+msg);

	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus msg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("idle  now ");
		session.write("hello server");

	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("session opened");
		session.write("hello world!");
		

	}

}
