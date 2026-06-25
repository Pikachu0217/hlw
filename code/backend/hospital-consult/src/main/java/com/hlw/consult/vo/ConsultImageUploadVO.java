package com.hlw.consult.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 问诊图片上传展示对象。
 */
@Getter
@Setter
public class ConsultImageUploadVO {
    /** 存储桶名称。 */
    private String bucket;
    /** 对象名称。 */
    private String objectName;
    /** 图片访问地址。 */
    private String url;
}
