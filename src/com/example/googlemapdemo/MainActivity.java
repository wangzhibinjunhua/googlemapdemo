package com.example.googlemapdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import junit.framework.Test;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.MapsInitializer;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected GoogleMap mGoogleMap;
	protected Button routeBtn;

	// 定义LocationManager对象
	private LocationManager locManager;
	private Location location;
	private String bestProvider;

	private CameraPosition cameraPosition;
	private MarkerOptions markerOpt;
	
	double dLong = 113.9433057;
	double dLat = 22.5315139;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.mgooglemap)).getMap();
		mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		// mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		//test
		//testgps();
		//test
		initProvider();
		getLocation();
		updateToNewLocation(location);

	}
	
	void testgps(double x,double y){
		MyLocation mlLocation=new MyLocation();
		mlLocation.lat=x;
		mlLocation.lng=y;
		
		MyLocation out =transformFromWGSToGCJ(mlLocation);
		dLat=out.lat;
		dLong=out.lng;
		Log.d("wzb","out.lat="+out.lat+"   out.lon="+out.lng);
	}

	void getLocation() {

		locManager.requestLocationUpdates(bestProvider, (long) 3 * 1000,
				(float) 8, new LocationListener() {

					@Override
					public void onStatusChanged(String provider, int status,
							Bundle extras) {
					}

					@Override
					public void onProviderEnabled(String provider) {
						// 当GPS LocationProvider可用时，更新位置
						location = locManager.getLastKnownLocation(provider);
					}

					@Override
					public void onProviderDisabled(String provider) {
						updateToNewLocation(null);
					}

					@Override
					public void onLocationChanged(Location location) {
						// 当GPS定位信息发生改变时，更新位置
						Log.d("wzb", "onLocationchanged");
						updateToNewLocation(location);
					}
				});

	}

	private void initProvider() {
		// 创建LocationManager对象
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// List all providers:
		List<String> providers = locManager.getAllProviders();
		Criteria criteria = new Criteria();
		bestProvider = locManager.getBestProvider(criteria, false);
		// bestProvider=locManager.NETWORK_PROVIDER;
		location = locManager.getLastKnownLocation(bestProvider);
		if (location == null) {
			bestProvider = locManager.NETWORK_PROVIDER;
			location = locManager.getLastKnownLocation(bestProvider);
		}
		Log.d("wzb", "location=" + location);
	}

	private void updateToNewLocation(Location location) {
		mGoogleMap.clear();
		markerOpt = new MarkerOptions();
		// 定位
		dLong = 113.9433057;
		dLat = 22.5315139;
		if (location != null) {
			// 获取经度
			 dLong = location.getLongitude();
			// 获取纬度
			 dLat = location.getLatitude();
			Log.d("wzb",
					"long:" + location.getLatitude() + "  lat:"
							+ location.getLongitude());
			testgps((double)location.getLatitude(), (double)location.getLongitude());

		}
		markerOpt.position(new LatLng(dLat, dLong));
		markerOpt.draggable(false);
		markerOpt.visible(true);
		markerOpt.anchor(0.5f, 0.5f);// 设为图片中心
		markerOpt.icon(BitmapDescriptorFactory
				.fromResource(android.R.drawable.ic_menu_mylocation));
		mGoogleMap.addMarker(markerOpt);
		// 将摄影机移动到指定的地理位置
		cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(dLat, dLong)) // Sets the center of the map
													// to ZINTUN
				.zoom(17) // 缩放比例
				.bearing(0) // Sets the orientation of the camera to east
				.tilt(30) // Sets the tilt of the camera to 30 degrees
				.build(); // Creates a CameraPosition from the builder
		mGoogleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}
	
	//add by wzb for wgs84 to wcj
	public static final double a = 6378245.0;
	public static final double ee = 0.00669342162296594323;
	public static final double pi = 3.14159265358979324;
	
	public class MyLocation{
		double lat;
		double lng;
	}
	private boolean outOfChina(double lat, double lng){
	    if (lng < 72.004 || lng > 137.8347)
	        return true;
	    
	    if (lat < 0.8293 || lat > 55.8271)
	        return true;
	    
	    return false;
	}
	
	private double transformLat(double x, double y)
	{
	    double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(x > 0 ? x:-x);
	    ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 *Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
	    ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
	    ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
	    
	    return ret;
	}
	
	double transformLon(double x, double y)
	{
	    double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(x > 0 ? x:-x);
	    ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
	    ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
	    ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
	    
	    return ret;
	}
	
	MyLocation LocationMake(double lat, double lng)
	{
		MyLocation loc=new MyLocation();
	    loc.lat = lat;
	    loc.lng = lng;
	    
	    return loc;
	}
	
	MyLocation transformFromWGSToGCJ(MyLocation wgLoc)
	{
		MyLocation mgLoc=new MyLocation();
	    if (outOfChina(wgLoc.lat, wgLoc.lng))
	    {
	        mgLoc = wgLoc;
	        return mgLoc;
	    }
	    double dLat = transformLat(wgLoc.lng - 105.0, wgLoc.lat - 35.0);
	    double dLon = transformLon(wgLoc.lng - 105.0, wgLoc.lat - 35.0);
	    double radLat = wgLoc.lat / 180.0 * pi;
	    double magic = Math.sin(radLat);
	    magic = 1 - ee * magic * magic;
	    double sqrtMagic = Math.sqrt(magic);
	    dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
	    dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
	    mgLoc.lat = wgLoc.lat + dLat;
	    mgLoc.lng = wgLoc.lng + dLon;
	    
	    return mgLoc;
	}
	
	
	
	
	
	
	
	

}
