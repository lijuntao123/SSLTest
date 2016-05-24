package com.example.ssltest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.avro.AvroRemoteException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.eastsoft.router.ipc.proto.MobileInfo;
import com.eastsoft.router.ipc.proto.RouterResult;
import com.eastsoft.router.ipc.proto.ServiceException;
import com.eastsoft.router.ipc.rpc.RouterClient;
import com.eastsoft.router.ipc.sslfilter.SSLContextGenerator;

public class MainActivity extends Activity {

	private static final int REMORT_PORT = 5000;
	private final String ip="129.1.1.124";
	private Button sendBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sendBtn=(Button)findViewById(R.id.button1);
		sendBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				MyThread mt=new MyThread();
				mt.start();
				System.out.println("++++++++++++send+++++++++");
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
//	public static void main(String[] args) {
//		new MainActivity().minaConnect();
//	}
	class MyThread extends Thread{

		@Override
		public void run() {
			minaConnect();   
			
//			requestRouter();
		}
		
	}
	
	public RouterResult requestRouter(){
		Context context=getApplicationContext();
		Long gid=1000L;
		InputStream in=context.getResources().openRawResource(R.raw.mobile_3);
		RouterClient routerClient=null;
		try {	
			List<SocketAddress> addrs=new ArrayList<SocketAddress>();
			addrs.add(new InetSocketAddress(ip,REMORT_PORT));
//			SSLContext sslContext =SSLContextGenerator.getSslContext(in, "eastsof");
			routerClient=RouterClient.getMobileRouterClient(addrs, null, in, 0);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		MobileInfo mInfo=new MobileInfo();	
		mInfo.setGid(gid);
//		mInfo.setGid(gid);
		addPhoneInfo(context, mInfo);		
		RouterResult routerResult=null;
		try {
			System.out.println("send request!");
			routerResult = routerClient.requestMobileLogin(mInfo);				
			
		} catch (ServiceException e) {				
			System.out.println(e.getErrorCode()+"=="+e.getDescription());		
			e.printStackTrace();
			
		} catch (AvroRemoteException e) {			
			e.printStackTrace();
			
		}finally{
			System.out.println("close connection!");
			routerClient.close();
		}
		return routerResult;
		
	}
	
	public void addPhoneInfo(Context context,MobileInfo mobileInfo){
		TelephonyManager phoneMgr=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);		
		String telNumber=phoneMgr.getLine1Number();//本机电话号码
		mobileInfo.setOptionalTelNumber(telNumber);
		mobileInfo.setOptionalIMEI(phoneMgr.getDeviceId());//手机IMEI
		mobileInfo.setOptionalMobileType(Build.MODEL);//手机型号	
	}
	
	public void minaConnect(){
		IoConnector connector = new NioSocketConnector();
		connector.getSessionConfig().setReadBufferSize(2048);
        InputStream in=getApplicationContext().getResources().openRawResource(R.raw.mobile_3);
		SSLContext sslContext = SSLContextGenerator.getSslContext(in, "123456");
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		SSLContext sslContext=getSSLContext(getApplicationContext());
		System.out.println("SSLContext protocol is: "+ sslContext.getProtocol());

		SslFilter sslFilter = new SslFilter(sslContext);
		sslFilter.setUseClientMode(true);
		connector.getFilterChain().addFirst("sslFilter", sslFilter);

		connector.getFilterChain().addLast("logger", new LoggingFilter());
		connector.getFilterChain().addLast("codec",	new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));

		connector.setHandler(new SSLClientHandler());
		connector.getSessionConfig().setUseReadOperation(true);
		connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		ConnectFuture future = connector.connect(new InetSocketAddress(ip, REMORT_PORT));
		System.out.println("=============");
	}

	@SuppressWarnings("resource")
	public static byte[] getKeyStore() {
		File file = new File("conf/mobile_1.bks");
		InputStream is;
		byte[] b = null;
		try {
			is = new FileInputStream(file);
			b = new byte[is.available()];
			is.read(b);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return b;
	}
	
	public SSLContext getSSLContext(Context mContext) {

		try {
			String passwd = "123456";
			
			InputStream caInput = mContext.getResources().openRawResource(R.raw.mobile_3);
			
			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(caInput, passwd.toCharArray());

			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			// ================
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, passwd.toCharArray());
			// ================

			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);

			// Create an SSLContext that uses our TrustManager
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);
			return context;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	/*public SSLContext getSSLContext(Context mContext) {

		try {
			String passwd = "eastsof";
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			// From
			// https://www.washington.edu/itconnect/security/ca/load-der.crt
			InputStream caInput = mContext.getResources().openRawResource(R.raw.ca);
			Certificate ca;
			try {
				ca = cf.generateCertificate(caInput);
				System.out.println("ca="+ ((X509Certificate) ca).getSubjectDN());
			} finally {
				caInput.close();
			}

			InputStream serInput = mContext.getResources().openRawResource(R.raw.server);
			Certificate ser;
			try {
				ser = cf.generateCertificate(serInput);
				System.out.println("ser="
						+ ((X509Certificate) ser).getSubjectDN());
			} finally {
				serInput.close();
			}
			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);

//			keyStore.setCertificateEntry("ca", ca);
//			keyStore.setCertificateEntry("ser", ser);
			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();

			// ================
			KeyManagerFactory kmf = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, passwd.toCharArray());
			// ================

			TrustManagerFactory tmf = TrustManagerFactory
					.getInstance(tmfAlgorithm);
			tmf.init(keyStore);

			// Create an SSLContext that uses our TrustManager
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);
			return context;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}*/

}
