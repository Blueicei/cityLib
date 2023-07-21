package com.lib.citylib.camTra.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 
 * @TableName camtrajectory
 */
@ApiModel(value = "CamTrajectory",description = "这个类定义了轨迹点的所有属性")
@TableName(value ="camtrajectory")
@Data
public class CamTrajectory implements Serializable {
    /**
     * 
     */
    private String carNumber;
    private String carType;

    /**
     * 
     */
    private String camId;

    /**
     * 
     */
    private String direction;

    /**
     * 
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date photoTime;

    /**
     * 
     */
    private Double camLon;

    /**
     * 
     */
    private Double camLat;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        CamTrajectory other = (CamTrajectory) that;
        return (this.getCarNumber() == null ? other.getCarNumber() == null : this.getCarNumber().equals(other.getCarNumber()))
            && (this.getCamId() == null ? other.getCamId() == null : this.getCamId().equals(other.getCamId()))
            && (this.getDirection() == null ? other.getDirection() == null : this.getDirection().equals(other.getDirection()))
            && (this.getPhotoTime() == null ? other.getPhotoTime() == null : this.getPhotoTime().equals(other.getPhotoTime()))
            && (this.getCamLon() == null ? other.getCamLon() == null : this.getCamLon().equals(other.getCamLon()))
            && (this.getCamLat() == null ? other.getCamLat() == null : this.getCamLat().equals(other.getCamLat()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCarNumber() == null) ? 0 : getCarNumber().hashCode());
        result = prime * result + ((getCamId() == null) ? 0 : getCamId().hashCode());
        result = prime * result + ((getDirection() == null) ? 0 : getDirection().hashCode());
        result = prime * result + ((getPhotoTime() == null) ? 0 : getPhotoTime().hashCode());
        result = prime * result + ((getCamLon() == null) ? 0 : getCamLon().hashCode());
        result = prime * result + ((getCamLat() == null) ? 0 : getCamLat().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", carNumber=").append(carNumber);
        sb.append(", carType=").append(carType);
        sb.append(", camId=").append(camId);
        sb.append(", direction=").append(direction);
        sb.append(", photoTime=").append(photoTime);
        sb.append(", camLon=").append(camLon);
        sb.append(", camLat=").append(camLat);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}