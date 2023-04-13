package com.xu.utils;

public class YmlNode {
        /**
         * 层级关系
         */
        private Integer level;
        /**
         * 键
         */
        private String key;
        /**
         * 值
         */
        private String value;
        /**
         * 是否为空行
         */
        private Boolean emptyLine;
        /**
         * 当前行是否为有效配置
         */
        private Boolean effective;
        /**
         * 头部注释（单行注释）
         */
        private String headRemark;
        /**
         * 末尾注释
         */
        private String tailRemark;
        /**
         * 是否为最后一层配置
         */
        private Boolean last;

        public Boolean getLast() {
            return last;
        }

        public void setLast(Boolean last) {
            this.last = last;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Boolean getEmptyLine() {
            return emptyLine;
        }

        public void setEmptyLine(Boolean emptyLine) {
            this.emptyLine = emptyLine;
        }

        public Boolean getEffective() {
            return effective;
        }

        public void setEffective(Boolean effective) {
            this.effective = effective;
        }

        public String getHeadRemark() {
            return headRemark;
        }

        public void setHeadRemark(String headRemark) {
            this.headRemark = headRemark;
        }

        public String getTailRemark() {
            return tailRemark;
        }

        public void setTailRemark(String tailRemark) {
            this.tailRemark = tailRemark;
        }
}
