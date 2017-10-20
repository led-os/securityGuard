package com.vidmt.telephone.utils;

import java.util.List;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.utils.Enums.VipType;
import com.vidmt.telephone.vos.LvlVo;

public class VipInfoUtil {
	private static List<LvlVo> ALL_LVL;
	private static int REY_TRY_TIMES = 3;

	/**
	 * 初始化vip信息
	 */
	public static boolean init() {
		if (ALL_LVL != null) {
			return true;
		}
		for (int i = 0; i < REY_TRY_TIMES; i++) {
			try {
				ALL_LVL = HttpManager.get().getLvl();
				return true;
			} catch (VidException e) {
				VLog.e("test", e);
			}
		}
		return false;
	}

	public static LvlVo getLvl(VipType type) {
		//huawei add start;
		if(ALL_LVL == null){
			init();
		}
		//huawei add end
		for (LvlVo lvl : ALL_LVL) {
			if (lvl.getType().equals(type.name())) {
				return lvl;
			}
		}
		return null;
	}
}
