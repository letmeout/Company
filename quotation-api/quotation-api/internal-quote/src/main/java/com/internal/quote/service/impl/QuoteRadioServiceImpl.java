package com.internal.quote.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.internal.common.core.domain.entity.SysDept;
import com.internal.common.core.domain.entity.SysUser;
import com.internal.common.utils.DateUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.internal.common.utils.StrUtil;
import com.internal.quote.domain.QuoteOpportunities;
import com.internal.quote.dto.BaseDTO;
import com.internal.quote.mapper.QuoteOpportunitiesMapper;
import com.internal.quote.vo.SysDeptVO;
import com.internal.system.mapper.SysDeptMapper;
import com.internal.system.mapper.SysUserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.internal.quote.mapper.QuoteRadioMapper;
import com.internal.quote.domain.QuoteRadio;
import com.internal.quote.service.IQuoteRadioService;

/**
 * 报价系统-报价部门占比Service业务层处理
 * 
 * @author internal
 * @date 2025-01-07
 */
@Service
@AllArgsConstructor
public class QuoteRadioServiceImpl extends ServiceImpl<QuoteRadioMapper, QuoteRadio> implements IQuoteRadioService
{

    private final QuoteRadioMapper quoteRadioMapper;
    private final QuoteOpportunitiesMapper quoteMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    /**
     * 查询报价系统-报价部门占比
     * 
     * @param id 报价系统-报价部门占比主键
     * @return 报价系统-报价部门占比
     */
    @Override
    public QuoteRadio selectQuoteRadioById(Long id)
    {
        return quoteRadioMapper.selectQuoteRadioById(id);
    }

    /**
     * 查询报价系统-报价部门占比列表
     * 
     * @param quoteRadio 报价系统-报价部门占比
     * @return 报价系统-报价部门占比
     */
    @Override
    public List<QuoteRadio> selectQuoteRadioList(QuoteRadio quoteRadio)
    {
        return quoteRadioMapper.selectQuoteRadioList(quoteRadio);
    }

    /**
     * 新增报价系统-报价部门占比
     * 
     * @param quoteRadio 报价系统-报价部门占比
     * @return 结果
     */
    @Override
    public int insertQuoteRadio(QuoteRadio quoteRadio)
    {
        quoteRadio.setCreateTime(DateUtils.getNowDate());
        return quoteRadioMapper.insert(quoteRadio);
    }

    /**
     * 修改报价系统-报价部门占比
     * 
     * @param quoteRadio 报价系统-报价部门占比
     * @return 结果
     */
    @Override
    public int updateQuoteRadio(QuoteRadio quoteRadio)
    {
        quoteRadio.setUpdateTime(DateUtils.getNowDate());
        return quoteRadioMapper.updateById(quoteRadio);
    }

    /**
     * 批量删除报价系统-报价部门占比
     * 
     * @param ids 需要删除的报价系统-报价部门占比主键
     * @return 结果
     */
    @Override
    public int deleteQuoteRadioByIds(Long[] ids)
    {
        return quoteRadioMapper.deleteQuoteRadioByIds(ids);
    }

    /**
     * 删除报价系统-报价部门占比信息
     * 
     * @param id 报价系统-报价部门占比主键
     * @return 结果
     */
    @Override
    public int deleteQuoteRadioById(Long id)
    {
        return quoteRadioMapper.deleteQuoteRadioById(id);
    }

    @Override
    public List<SysDeptVO> getQuoteDept(BaseDTO dto) {
        QuoteOpportunities quote = quoteMapper.selectById(dto.getOpportunitiesId());
        if(ObjectUtil.isEmpty(quote)){
            throw new RuntimeException("无效的报价信息");
        }

        List<String> crmIdList = StrUtil.splitList(quote.getSupportPerson(),",");
        if(CollUtil.isEmpty(crmIdList)){
            return new LinkedList<>();
        }
        List<SysUser> userList = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                .in(SysUser::getCrmUserId,crmIdList));

        List<Long> deptIdList = userList.stream().map(SysUser::getDeptId).collect(Collectors.toList());
        if(CollUtil.isEmpty(deptIdList)){
            return new LinkedList<>();
        }
        List<SysDept> deptList = deptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
                .in(SysDept::getDeptId,deptIdList));

        List<SysDeptVO> result = BeanUtil.copyToList(deptList,SysDeptVO.class);

        result.forEach(dept ->{
            List<SysUser> users = userList.stream().filter(x -> x.getDeptId().equals(dept.getDeptId())).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(users)){
                dept.setUserNames(
                        String.join(",",
                                users.stream()
                                        .filter(x -> ObjectUtil.isNotEmpty(x.getNickName()))
                                        .map(x -> x.getNickName())
                                        .collect(Collectors.toList())
                        )
                );
                dept.setCrmUserIds(
                        String.join(",",
                                users.stream()
                                        .filter(x -> ObjectUtil.isNotEmpty(x.getCrmUserId()))
                                        .map(x -> x.getCrmUserId())
                                        .collect(Collectors.toList())
                        )
                );
            }
        });

        return result;
    }
}
