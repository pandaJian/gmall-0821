package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> queryByCidPage(Long cid) {
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id",cid);
        return this.list(queryWrapper);
    }

    @Override
    public List<AttrGroupEntity> queryByCid(Long catId) {

        //根据分类id查询分组
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id",catId);
        List<AttrGroupEntity> attrGroupEntityList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(attrGroupEntityList)){
            return null;
        }
        //遍历分组的id查询组下的规格参数
        attrGroupEntityList.forEach(attrGroupEntity -> {
            QueryWrapper<AttrEntity> attrEntityQueryWrapper = new QueryWrapper<>();
            attrEntityQueryWrapper.eq("group_id",attrGroupEntity.getId()).eq("type",1);
            List<AttrEntity> attrEntities = attrMapper.selectList(attrEntityQueryWrapper);
            attrGroupEntity.setAttrEntities(attrEntities);
        });
        return attrGroupEntityList;
    }

}