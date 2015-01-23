package com.seven.quickphone;


import android.os.Handler;
import android.view.ViewGroup;

import com.wandoujia.ads.sdk.Ads;
import com.wandoujia.ads.sdk.loader.Fetcher;

public class AD {

	public static MainActivity ma;


	public static final String ADS_APP_ID = "100011982";
	public static final String ADS_SECRET_KEY = "9b50e271a65f8ebbb916e689f7359883";
	public static final String TAG_INTERSTITIAL_BANNER = "0808058a2c4354245bde1b039470e75f";
	public static Runnable showWandoujia = new Runnable() {
		public void run() {
			 if (Ads.isLoaded(Fetcher.AdFormat.banner, TAG_INTERSTITIAL_BANNER)) 
			 {
				 
				 ma.adBanner = Ads.showBannerAd(ma, (ViewGroup)ma.view4.findViewById(R.id.banner_ad_container),
						 TAG_INTERSTITIAL_BANNER);
				 ma.adBanner.startAutoScroll();
			    }
		}
	};
	
	public static void showWandoujia()
	{
		new Handler().post(showWandoujia);
	}
	

}
