<template>
  <div class="grid grid-cols-4">
    <div class="col-start-1">
      <a-button
        v-permission="['PROJECT_ENVIRONMENT:READ+UPDATE']"
        type="outline"
        :disabled="uploadIsDisable"
        @click="handleAdd"
        >{{ t('点击上传') }}</a-button
      >
    </div>
  </div>
  <div class="tip">证书文件</div>
  <MsBaseTable class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
    <template #operation="{ record }">
      <div class="flex flex-row flex-nowrap items-center">
        <MsButton class="!mr-0" :disabled="isDisabled" @click="handleSingleDelete(record)">{{
          t('common.delete')
        }}</MsButton>
        <a-divider class="h-[16px]" direction="vertical" />
        <MsButton class="!mr-0" :disabled="isDisabled" @click="handleEdit(record)">{{ t('common.edit') }}</MsButton>
        <a-divider class="h-[16px]" direction="vertical" />
      </div>
    </template>
    <template v-if="''.trim() === ''" #empty>
      <div class="flex w-full items-center justify-center text-[var(--color-text-4)]">
        <span v-if="hasAnyPermission(['PROJECT_ENVIRONMENT:READ+UPDATE'])">{{ t('暂无数据') }}</span>
      </div>
    </template>
  </MsBaseTable>

  <!--  <p class="tip">证书条目</p>-->
  <!--  <MsBaseTable class="mt-[16px]" v-bind="propsRes1" v-on="propsEvent1">-->
  <!--    <template #default="{ record }">-->
  <!--      <div class="flex flex-row flex-nowrap items-center">-->
  <!--                <MsCheckbox-->
  <!--                    @change="rowSelectChange(record)"-->
  <!--                    :indeterminate="getIndeterminate(record)"/>-->
  <!--      </div>-->
  <!--    </template>-->
  <!--    <template v-if="''.trim() === ''" #empty>-->
  <!--      <div class="flex w-full items-center justify-center text-[var(&#45;&#45;color-text-4)]">-->
  <!--        <span v-if="hasAnyPermission(['PROJECT_ENVIRONMENT:READ+UPDATE'])">{{-->
  <!--          t('caseManagement.caseReview.tableNoData')-->
  <!--        }}</span>-->
  <!--        <span v-else>{{ t('caseManagement.featureCase.tableNoData') }}</span>-->
  <!--      </div>-->
  <!--    </template>-->
  <!--  </MsBaseTable>-->

  <AddSSLFile v-model:visible="addVisible" :current-id="currentId" @close="addVisible = false" />
</template>

<script lang="ts" async setup>
  import { TableData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCheckbox from '@/components/pure/ms-checkbox/MsCheckbox.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import AddDatabaseModal from '@/views/project-management/environmental/components/envParams/popUp/addDatabaseModal.vue';
  import AddSSLFile from '@/views/project-management/environmental/components/envParams/popUp/addSSLFile.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';
  import { hasAnyPermission } from '@/utils/permission';

  import { BugListItem } from '@/models/bug-management';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const store = useProjectEnvStore();

  const innerParam = computed(() => store.currentEnvDetailInfo.config.keyStoreConfig.files || []);

  const keyword = ref('');
  const tableStore = useTableStore();
  const addVisible = ref(false);
  const currentId = ref('');
  const filesColumns: MsTableColumn = [
    {
      title: '文件名',
      dataIndex: 'name',
      showTooltip: true,
      showDrag: false,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: '类型',
      dataIndex: 'type',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: '密码',
      dataIndex: 'password',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      showDrag: true,
      showInTable: false,
      showTooltip: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 170,
    },
  ];
  await tableStore.initColumn(TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_SSL, filesColumns);
  const { propsRes, propsEvent } = useTable(undefined, {
    tableKey: TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_SSL,
    scroll: { x: '100%' },
    selectable: false,
    showSetting: true,
    showPagination: false,
    heightUsed: 590,
    showMode: false,
  });

  const entrysColumns: MsTableColumn = [
    {
      title: '原有别名',
      dataIndex: 'originalName',
      showTooltip: true,
      showDrag: false,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: '新别名',
      dataIndex: 'newName',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'type',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: '密码',
      dataIndex: 'password',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: '来源',
      dataIndex: 'sourceName',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: '是否默认',
      dataIndex: 'default',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
    },
  ];
  // await tableStore.initColumn(TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_SSL_ENTRY, entrysColumns);
  // const { propsRes1, propsEvent1 } = useTable(undefined, {
  //   tableKey: TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_SSL_ENTRY,
  //   scroll: { x: '100%' },
  //   selectable: false,
  //   showSetting: true,
  //   showPagination: false,
  //   heightUsed: 590,
  //   showMode: false,
  // });
  const uploadIsDisable = computed(() => {
    if (innerParam.value.length > 0) {
      return true;
    }
    return false;
  });

  const isDisabled = computed(() => !hasAnyPermission(['PROJECT_ENVIRONMENT:READ+UPDATE']));

  const handleSingleDelete = (record?: TableData) => {
    if (record) {
      const index = innerParam.value.findIndex((item) => item.id === record.id);
      // 删除相应条目
      if (store.currentEnvDetailInfo.config.keyStoreConfig.entry) {
        store.currentEnvDetailInfo.config.keyStoreConfig.entry.forEach((entry) => {
          if (entry && entry.sourceId === innerParam.value[index].id) {
            const entryIndex = store.currentEnvDetailInfo.config.keyStoreConfig.entry.findIndex(
              (d) => d.sourceId === innerParam.value[index].id
            );
            if (entryIndex !== -1) {
              store.currentEnvDetailInfo.config.keyStoreConfig.entry.splice(index, 1);
            }
          }
        });
      }
      // 删除证书
      innerParam.value.splice(index, 1);
    }
  };

  /**
   * 编辑
   */
  const handleEdit = (record: any) => {
    currentId.value = record.id;
    addVisible.value = true;
  };

  /**
   * 添加
   */
  const handleAdd = () => {
    currentId.value = '';
    addVisible.value = true;
  };

  const rowSelectChange = (record: TableData) => {};

  function getIndeterminate(record: TableData) {}

  /**
   * 查询
   */
  const fetchData = () => {
    // if (keyword.value) {
    //   propsRes.value.data = innerParam.value.filter((item) => item.dataSource.includes(keyword.value));
    // } else {
    //   propsRes.value.data = innerParam.value;
    // }
  };

  watch(
    () => innerParam.value,
    (val) => {
      if (val) {
        propsRes.value.data = val;
      }
    },
    {
      deep: true,
      immediate: true,
    }
  );
</script>

<style lang="less" scoped>
  .title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 0 16px;
    height: 38px;
    border: 1px solid rgb(var(--primary-5));
    border-radius: 4px;
    background-color: rgb(var(--primary-1));
  }
</style>
