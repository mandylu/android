package com.baixing.sharing;

import com.baixing.entity.Ad;

interface BaseSharingManager{
	public void share(Ad ad);
	public void release();
}