import { TableSortable } from '@arco-design/web-vue';
import { cloneDeep } from 'lodash-es';

import type { MsTableColumn } from '@/components/pure/ms-table/type';

import type { countDetail } from '@/models/testPlan/testPlanReport';
import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

import { casePriorityOptions, lastReportStatusListOptions } from '@/views/api-test/components/config';
import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';

export function getSummaryDetail(detailCount: countDetail) {
  if (detailCount) {
    const { success, error, fakeError, pending, block } = detailCount;
    // 已执行用例
    const hasExecutedCase = success + error + fakeError + block;
    // 用例总数
    const caseTotal = hasExecutedCase + pending;
    // 执行率
    const executedCount = (hasExecutedCase / caseTotal) * 100;
    const apiExecutedRate = `${Number.isNaN(executedCount) ? 0 : executedCount.toFixed(2)}%`;
    // 通过率
    const successCount = (success / caseTotal) * 100;
    const successRate = `${Number.isNaN(successCount) ? 0 : successCount.toFixed(2)}%`;
    return {
      hasExecutedCase,
      caseTotal,
      apiExecutedRate,
      successRate,
      pending,
      success,
    };
  }
  return {
    hasExecutedCase: 0,
    caseTotal: 0,
    apiExecutedRate: 0,
    successRate: 0,
    pending: 0,
    success: 0,
  };
}

export default {};
