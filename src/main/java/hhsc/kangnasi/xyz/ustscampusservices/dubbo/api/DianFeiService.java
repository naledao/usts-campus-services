package hhsc.kangnasi.xyz.ustscampusservices.dubbo.api;


import dianfei.Dianfei;

public interface DianFeiService {
    // 方法名需与 proto 中 rpc 完全一致（大小写也一致）
    Dianfei.QueryReply QueryCurrentElectricity(Dianfei.QueryRequest request);
}
