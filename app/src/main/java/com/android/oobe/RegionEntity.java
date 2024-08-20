package com.android.oobe;


import me.yokeyword.indexablerv.IndexableEntity;
import me.yokeyword.indexablerv.PinyinUtil;

public class RegionEntity implements IndexableEntity {
    private RegionInfo mRegionInfo;
    private String mRegionNamePingYin;

    public RegionEntity(RegionInfo regionInfo) {
        mRegionInfo = regionInfo;
        mRegionNamePingYin = PinyinUtil.getPingYin(regionInfo.getName());
    }

    public String getRegionName() {
        return mRegionInfo.getName();
    }

    public RegionInfo getRegionInfo() {
        return mRegionInfo;
    }

    public String getRegionNamePingYin() {
        return mRegionNamePingYin;
    }

    @Override
    public String getFieldIndexBy() {
        return getRegionName();
    }

    @Override
    public void setFieldIndexBy(String name) {
    }

    @Override
    public void setFieldPinyinIndexBy(String pinYin) {
    }
}
