package com.baixing.sharing;

import com.baixing.entity.Ad;

public interface BaseSharingManager{
	public void share(Ad ad);
	public void auth();
	public void release();
}