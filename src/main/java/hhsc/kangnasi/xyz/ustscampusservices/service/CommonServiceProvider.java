package hhsc.kangnasi.xyz.ustscampusservices.service;

import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.CommonServiceVo;

import java.util.List;

/**
 * Provides service summaries for the common-service aggregate endpoint.
 *
 * <p>Implementations are injected and invoked directly so this path remains
 * compatible with GraalVM native images without reflective method lookup.</p>
 */
public interface CommonServiceProvider {

    List<CommonServiceVo> allService(String email);
}
