package com.android.provision;

import java.io.Serializable;
import java.util.List;

public class AppListResult {
    private int code;
    private String message;
    private DataBeanX data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBeanX getData() {
        return data;
    }

    public void setData(DataBeanX data) {
        this.data = data;
    }

    public static class DataBeanX implements Serializable {
        private PageBean page;
        private List<DataBean> data;
        public PageBean getPage() {
            return page;
        }

        public void setPage(PageBean page) {
            this.page = page;
        }
        public List<DataBean> getData() {
            return data;
        }

        public void setData(List<DataBean> data) {
            this.data = data;
        }

        public static class PageBean implements Serializable {
            private boolean page_enable;
            private int total;
            private int page;
            private int page_size;

            public boolean isPage_enable() {
                return page_enable;
            }

            public void setPage_enable(boolean page_enable) {
                this.page_enable = page_enable;
            }

            public int getTotal() {
                return total;
            }

            public void setTotal(int total) {
                this.total = total;
            }

            public int getPage() {
                return page;
            }

            public void setPage(int page) {
                this.page = page;
            }

            public int getPage_size() {
                return page_size;
            }

            public void setPage_size(int page_size) {
                this.page_size = page_size;
            }
        }
        public static class DataBean implements Serializable {
            private int port;
            private int id;
            private String Type;
            private String Path;
            private String Icon;
            private String IconPath;
            private String IconType;
            private String Name;
            private String ZhName;
            private boolean checked;

            public boolean isChecked() {
                return checked;
            }

            public void setChecked(boolean checked) {
                this.checked = checked;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getType() {
                return Type;
            }

            public void setType(String type) {
                Type = type;
            }

            public String getPath() {
                return Path;
            }

            public void setPath(String path) {
                Path = path;
            }

            public String getIcon() {
                return Icon;
            }

            public void setIcon(String icon) {
                Icon = icon;
            }

            public String getIconPath() {
                return IconPath;
            }

            public void setIconPath(String iconPath) {
                IconPath = iconPath;
            }

            public String getIconType() {
                return IconType;
            }

            public void setIconType(String iconType) {
                IconType = iconType;
            }

            public String getName() {
                return Name;
            }

            public void setName(String name) {
                Name = name;
            }

            public String getZhName() {
                return ZhName;
            }

            public void setZhName(String zhName) {
                ZhName = zhName;
            }

            @Override
            public String toString() {
                return "DataBean{" +
                        "port=" + port +
                        ", id=" + id +
                        ", Type='" + Type + '\'' +
                        ", Path='" + Path + '\'' +
                        ", Icon='" + Icon + '\'' +
                        ", IconPath='" + IconPath + '\'' +
                        ", IconType='" + IconType + '\'' +
                        ", Name='" + Name + '\'' +
                        ", ZhName='" + ZhName + '\'' +
                        '}';
            }
        }
    }
}
