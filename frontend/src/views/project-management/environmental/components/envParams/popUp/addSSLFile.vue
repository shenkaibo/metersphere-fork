<template>
  <a-modal
    v-model:visible="currentVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    unmount-on-close
    :modal-style="{
      top: '0 !important',
    }"
    @cancel="handleCancel(false)"
  >
    <template #title>
      <span v-if="props.currentId">
        {{ t('更新配置') }}
      </span>
      <span v-else>
        {{ t('上传文件') }}
      </span>
    </template>
    <div class="form">
      <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
        <a-form-item field="password" required :label="t('密码')" asterisk-position="end">
          <a-input v-model="form.password" :max-length="255" allow-clear :placeholder="t('')" />
        </a-form-item>

        <MsUpload
          v-model:file-list="fileList"
          accept="cert"
          :auto-upload="false"
          size-unit="MB"
          draggable
          :limit="2"
          class="w-full"
        >
          <template #subText>
            <div class="flex">
              {{ t('apiScenario.importScenarioUploadTip', { type: fileAccept, size: appStore.getFileMaxSize }) }}
            </div>
          </template>
        </MsUpload>
      </a-form>
    </div>
    <template #footer>
      <div class="flex flex-row justify-end">
        <div class="flex flex-row gap-[14px]">
          <a-button type="secondary" :loading="loading" @click="handleCancel(false)">
            {{ t('common.cancel') }}
          </a-button>
          <a-button type="primary" :loading="loading" :disabled="fileList.length === 0" @click="handleBeforeOk">
            {{ props.currentId ? t('common.confirm') : t('common.add') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsUpload from '@/components/pure/ms-upload/index.vue';

  import { getSslEntry, validateDatabaseEnv } from '@/api/modules/project-management/envManagement';
  import { importUserInfo } from '@/api/modules/setting/user';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';
  import { getGenerateId } from '@/utils';

  import { DataSourceItem, EnvSSLConfig, KeyStoreEntry, KeyStoreFile } from '@/models/projectManagement/environmental';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import { FileItem } from '@arco-design/web-vue/es/upload/interfaces';

  const { t } = useI18n();
  const store = useProjectEnvStore();
  const props = defineProps<{
    currentId: string;
    visible: boolean;
  }>();

  const formRef = ref<FormInstance>();

  const loading = ref(false);
  const appStore = useAppStore();

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
    (e: 'addOrUpdate', data: EnvSSLConfig, cb: (v: boolean) => void): void;
  }>();

  const currentVisible = defineModel('visible', {
    default: false,
    type: Boolean,
  });
  const fileList = ref<FileItem[]>([]);
  const entryList = ref<KeyStoreEntry[]>([]);
  const initForm = {
    id: '',
    name: '',
    type: '',
    updateTime: new Date().getTime(),
    password: '',
  };
  const fileAccept = computed(() => {
    return '.p12,.jks,.pfx';
  });

  const form = ref<KeyStoreFile>({
    ...initForm,
  });

  const formReset = () => {
    form.value = {
      id: '',
      name: '',
      type: '',
      updateTime: new Date().getTime(),
      password: '',
    };
  };
  const handleCancel = (shouldSearch: boolean) => {
    emit('cancel', shouldSearch);
    currentVisible.value = false;
    fileList.value = [];
    formReset();
  };

  const handleBeforeOk = async () => {
    // await formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {});
    console.log('确定');

    await formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      const isExist = store.currentEnvDetailInfo.config.keyStoreConfig.files.some(
        (item) => item.name === form.value.name && item.id !== form.value.id
      );
      if (isExist) {
        Message.error(t('文件已存在'));
        return;
      }
      try {
        loading.value = true;
        // entry 验证
        if (fileList.value.length > 0) {
          const entryItem: { request: string; fileList: File[] } = {
            request: form.value.password,
            fileList: fileList.value.map((item: any) => item.file),
          };

          const res = (await getSslEntry(entryItem)) || [];

          const index = store.currentEnvDetailInfo.config.keyStoreConfig.files.findIndex(
            (item: any) => item.id === form.value.id
          );
          if (index > -1) {
            // 替换文件
            const item = {
              ...form.value,
              name: fileList.value[0].file?.name,
              type: fileList.value[0].file?.type,
              file: fileList.value[0].file,
              updateTime: new Date().getTime(),
            };
            // 替换原证书
            store.currentEnvDetailInfo.config.keyStoreConfig.files.splice(index, 1);
            store.currentEnvDetailInfo.config.keyStoreConfig.files.push(item);
            // 删除原条目
            if (store.currentEnvDetailInfo.config.keyStoreConfig.entry) {
              store.currentEnvDetailInfo.config.keyStoreConfig.entry.forEach((entry) => {
                if (entry && entry.sourceId === item.id) {
                  const entryIndex = store.currentEnvDetailInfo.config.keyStoreConfig.entry.findIndex(
                    (d) => d.sourceId === item.id
                  );
                  if (entryIndex !== -1) {
                    store.currentEnvDetailInfo.config.keyStoreConfig.entry.splice(index, 1);
                  }
                }
              });
              // 新增条目
              // eslint-disable-next-line no-shadow
              res.data.forEach((entryItem: KeyStoreEntry) => {
                if (entryItem) {
                  entryItem.id = getGenerateId();
                  entryItem.sourceId = item.id;
                  entryItem.sourceName = item.name;
                }
                entryItem.password = '';
                entryItem.isDefault = false;
                store.currentEnvDetailInfo.config.keyStoreConfig.entry.push(entryItem);
              });
            }
          } else {
            // 新增文件
            const item = {
              ...form.value,
              id: getGenerateId(),
              name: fileList.value[0].file?.name,
              type: fileList.value[0].file?.type,
              file: fileList.value[0].file,
              updateTime: new Date().getTime(),
            };
            store.currentEnvDetailInfo.config.keyStoreConfig.files.push(item);
            // 新增条目
            res.data.forEach((entry: KeyStoreEntry) => {
              if (entry) {
                entry.id = getGenerateId();
                entry.sourceId = item.id;
                entry.sourceName = item.name;
              }
              entry.password = '';
              entry.isDefault = false;
              store.currentEnvDetailInfo.config.keyStoreConfig.entry.push(entry);
            });
          }
        }
        formReset();
        fileList.value = [];
        currentVisible.value = false;
        Message.success(t('添加成功'));
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        loading.value = false;
      }
    });
  };

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        if (props.currentId) {
          const currentItem = store.currentEnvDetailInfo.config.keyStoreConfig.files.find(
            (item) => item.id === props.currentId
          ) as KeyStoreFile;
          if (currentItem) {
            form.value = {
              ...currentItem,
            };
          } else {
            formReset();
          }
        }
      }
    }
  );
</script>

<style lang="less" scoped></style>
