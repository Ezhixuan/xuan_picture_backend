package org.ezhixuan.xuan_picture_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ezhixuan.xuan_picture_backend.factory.picture.PictureFactory;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureQueryRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureReviewRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadByBatchRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadRequest;
import org.ezhixuan.xuan_picture_backend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import org.ezhixuan.xuan_picture_backend.model.entity.User;
import org.ezhixuan.xuan_picture_backend.model.vo.picture.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author ezhixuan
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-12 15:28:33
*/
public interface PictureService extends IService<Picture> {

    /**
     * 获取图片工厂
     * @author Ezhixuan
     * @return PictureFactory
     */
    PictureFactory getPictureFactory();

    /**
     * 图片上传
     * @author Ezhixuan
     * @param file MultipartFile
     * @param request 请求参数
     * @param loginUser 用户信息
     * @return 图片脱敏后信息
     */
    PictureVO upload(MultipartFile file, PictureUploadRequest request, User loginUser);

    /**
     * 通过网络url上传图片
     * @author Ezhixuan
     * @param url 图片url
     * @param request 请求参数
     * @param loginUser 用户信息
     * @return 图片脱敏后信息
     */
    PictureVO uploadByUrl(String url, PictureUploadRequest request, User loginUser);

    /**
     * 批量抓取图片
     * @author Ezhixuan
     * @param batchRequest 抓取请求
     * @param loginUser 用户信息
     * @return 成功抓取数量
     */
    int uploadBatch(PictureUploadByBatchRequest batchRequest, User loginUser);

    /**
     * 获取脱敏后图片信息 包含对应用户信息
     * @author Ezhixuan
     * @param picture 图片
     * @return PictureVO
     */
    PictureVO getPictureVO(Picture picture);

    /**
     * 获取脱敏后图片信息 包含对应用户信息 批量
     * @author Ezhixuan
     * @param picturePage 需要已包含分页信息
     * @return Page<PictureVO>
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage);

    /**
     * 获取查询条件
     * @author Ezhixuan
     * @param queryRequest 查询参数
     * @return QueryWrapper<Picture>
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest queryRequest);

    /**
     * 校验图片信息
     * @author Ezhixuan
     * @param picture 图片信息
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     * @author Ezhixuan
     * @param reviewRequest 审核参数
     * @param loginUser 审核用户信息
     */
    void doPictureReview(PictureReviewRequest reviewRequest, User loginUser);

    /**
     * 填充待审核信息 管理员自动过审 普通用户编辑修改都必须再次经过审核
     * @author Ezhixuan
     * @param picture 图片
     * @param loginUser 用户
     */
    void fillReviewParams(Picture picture, User loginUser);
}
