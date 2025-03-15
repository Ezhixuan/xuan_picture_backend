package org.ezhixuan.xuan_picture_backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.RequiredArgsConstructor;
import org.ezhixuan.xuan_picture_backend.constant.UserConstant;
import org.ezhixuan.xuan_picture_backend.exception.ErrorCode;
import org.ezhixuan.xuan_picture_backend.factory.picture.PictureFactory;
import org.ezhixuan.xuan_picture_backend.factory.picture.impl.GitHubPictureServiceImpl;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureQueryRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureReviewRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadResult;
import org.ezhixuan.xuan_picture_backend.model.entity.Picture;
import org.ezhixuan.xuan_picture_backend.mapper.PictureMapper;
import org.ezhixuan.xuan_picture_backend.model.entity.User;
import org.ezhixuan.xuan_picture_backend.model.enums.PictureReviewStatusEnum;
import org.ezhixuan.xuan_picture_backend.model.vo.picture.PictureVO;
import org.ezhixuan.xuan_picture_backend.service.PictureService;
import org.ezhixuan.xuan_picture_backend.service.UserService;
import org.ezhixuan.xuan_picture_backend.utils.PictureCommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.ezhixuan.xuan_picture_backend.exception.ThrowUtils.throwIf;

/**
 * @author ezhixuan
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-03-12 15:28:33
 */
@Service
@RequiredArgsConstructor
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    private final PictureFactory factory;
    private final UserService userService;
    private final GitHubPictureServiceImpl gitHubPictureServiceImpl;

    /**
     * 获取图片工厂
     *
     * @return PictureFactory
     * @author Ezhixuan
     */
    @Override
    public PictureFactory getPictureFactory() {
        return factory;
    }

    /**
     * 图片上传
     *
     * @author Ezhixuan
     * @param file MultipartFile
     * @param request 请求参数
     * @param loginUser 用户信息
     * @return 图片脱敏后信息
     */
    @Override
    public PictureVO upload(MultipartFile file, PictureUploadRequest request, User loginUser) {
        validateUploadParam(request, loginUser);
        String targetPath = String.format("public/%s/", loginUser.getId());
        try {
            PictureUploadResult result = PictureCommonUtil.processImage(file, request.isNotReName());
            String url = factory.getInstance().doUpload(file.getInputStream(), targetPath, result.getName());
            result.setUrl(url);
            return doUpload(loginUser, result, request.getId());
        } catch (IOException | UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过网络url上传图片
     *
     * @param url       图片url
     * @param request   请求参数
     * @param loginUser 用户信息
     * @return 图片脱敏后信息
     * @author Ezhixuan
     */
    @Override
    public PictureVO uploadByUrl(String url, PictureUploadRequest request, User loginUser) {
        validateUploadParam(request, loginUser);
        PictureCommonUtil.validatePicture(url);
        // github doDownload实现为通过网络请求下载图片
        try(InputStream inputStream = gitHubPictureServiceImpl.doDownload(url)) {

            PictureUploadResult result = PictureCommonUtil.processImage(url, request.isNotReName());
            String targetPath = String.format("public/%s/", loginUser.getId());
            url = factory.getInstance().doUpload(inputStream, targetPath, result.getName());
            result.setUrl(url);
            return doUpload(loginUser, result, request.getId());
        } catch (IOException | UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检验上传请求参数
     * @author Ezhixuan
     * @param request
     * @param loginUser
     */
    private void validateUploadParam(PictureUploadRequest request, User loginUser) {
        throwIf(Objects.isNull(loginUser), ErrorCode.NO_AUTH_ERROR);
        if (Objects.nonNull(request) && Objects.nonNull(request.getId())) {
            Picture picture = this.getById(request.getId());
            throwIf(Objects.isNull(picture), ErrorCode.PARAMS_ERROR, "图片不存在");
            throwIf(!Objects.equals(picture.getUserId(), loginUser.getId()) || !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权限修改图片");
        }
    }

    /**
     * 获取上传后的uploadResult进行数据库存储
     * @author Ezhixuan
     * @param loginUser 上传用户
     * @param uploadResult 图片上传结果 所有{@link PictureUploadResult}参数都需要进行赋值
     * @param picId 图片id，允许为null
     * @return PictureVO 图片脱敏后信息
     */
    private PictureVO doUpload(User loginUser, PictureUploadResult uploadResult, Long picId) {
        // 内部方法 传入的参数都经过验证 不需要对uploadResult进行二次验证
        Picture picture = BeanUtil.copyProperties(uploadResult, Picture.class);
        picture.setUserId(loginUser.getId());
        if (Objects.nonNull(picId)) {
            picture.setId(picId);
            picture.setEditTime(new Date());
        }
        this.fillReviewParams(picture, loginUser);
        boolean resultSave = this.saveOrUpdate(picture);
        throwIf(!resultSave, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVO.objToVo(picture);
    }

    /**
     * 获取脱敏后图片信息 包含对应用户信息
     * @author Ezhixuan
     * @param picture 图片
     * @return PictureVO
     */
    @Override
    public PictureVO getPictureVO(Picture picture) {
        return toVoList(Collections.singletonList(picture)).stream().findAny().orElseThrow(() -> new RuntimeException("图片不存在"));
    }

    /**
     * 获取脱敏后图片信息 包含对应用户信息 批量
     * @author Ezhixuan
     * @param picturePage 需要已包含分页信息
     * @return Page<PictureVO>
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        pictureVOPage.setRecords(toVoList(pictureList));
        return pictureVOPage;
    }

    /**
     * picture 转 vo
     * @author Ezhixuan
     * @param pictures 图片列表
     * @return List<PictureVO>
     */
    private List<PictureVO> toVoList(Collection<Picture> pictures) {
        if (CollUtil.isEmpty(pictures)) {
            return new ArrayList<>();
        }
        Set<Long> userIds = pictures.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMapByIds = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, v -> v));
        List<PictureVO> voList = pictures.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        voList.forEach(vo -> {
            User user = userMapByIds.get(vo.getUserId());
            vo.setUser(userService.getUserVO(user));
        });
        return voList;
    }


    /**
     * 获取查询条件
     *
     * @param queryRequest 查询参数
     * @return QueryWrapper<Picture>
     * @author Ezhixuan
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest queryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (queryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = queryRequest.getId();
        String name = queryRequest.getName();
        String introduction = queryRequest.getIntroduction();
        String category = queryRequest.getCategory();
        List<String> tags = queryRequest.getTags();
        Long picSize = queryRequest.getPicSize();
        Integer picWidth = queryRequest.getPicWidth();
        Integer picHeight = queryRequest.getPicHeight();
        Double picScale = queryRequest.getPicScale();
        String picFormat = queryRequest.getPicFormat();
        String searchText = queryRequest.getSearchText();
        Long userId = queryRequest.getUserId();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        Integer reviewStatus = queryRequest.getReviewStatus();
        String reviewMessage = queryRequest.getReviewMessage();
        Long reviewerId = queryRequest.getReviewerId();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 校验图片信息
     * @author Ezhixuan
     * @param picture 图片信息
     */
    @Override
    public void validPicture(Picture picture) {
        throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    /**
     * 图片审核
     *
     * @param reviewRequest 审核参数
     * @param loginUser     审核用户信息
     * @author Ezhixuan
     */
    @Override
    public void doPictureReview(PictureReviewRequest reviewRequest, User loginUser) {
        // 权限校验
        throwIf(Objects.isNull(reviewRequest) || Objects.isNull(reviewRequest.getId()) || Objects.equals(reviewRequest.getReviewStatus(), PictureReviewStatusEnum.REVIEWING.getValue()), ErrorCode.PARAMS_ERROR);
        throwIf(Objects.isNull(loginUser) || !Objects.equals(loginUser.getUserRole(), UserConstant.USER_ROLE_ADMIN), ErrorCode.NO_AUTH_ERROR);
        // 获取图片信息
        Picture picture = this.getById(reviewRequest.getId());
        throwIf(Objects.isNull(picture), ErrorCode.NOT_FOUND_ERROR);
        throwIf(Objects.equals(picture.getReviewStatus(), reviewRequest.getReviewStatus()), ErrorCode.PARAMS_ERROR, "请勿重复审核");

        picture.setReviewStatus(reviewRequest.getReviewStatus());
        picture.setReviewMessage(reviewRequest.getReviewMessage());
        picture.setReviewerId(loginUser.getId());
        picture.setReviewTime(new Date());
        boolean res = this.updateById(picture);
        throwIf(!res, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 填充待审核信息 管理员自动过审 普通用户编辑修改都必须再次经过审核
     *
     * @param picture   图片
     * @param loginUser 用户
     * @author Ezhixuan
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动审核通过");
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
        }else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
            picture.setReviewMessage("");
            picture.setReviewerId(-1L);
        }
    }
}
