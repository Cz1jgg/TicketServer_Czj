<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header">
        <span class="card-title">创建工单</span>
        <div class="back-btn">
          <el-button @click="goBack">返回</el-button>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工单标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入工单标题" />
        </el-form-item>

        <el-form-item label="工单类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择工单类型">
            <el-option v-for="type in ticketTypes" :key="type.id" :label="type.typeName" :value="type.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="紧急程度" prop="urgency">
          <el-radio-group v-model="form.urgency">
            <el-radio :label="1">低</el-radio>
            <el-radio :label="2">中</el-radio>
            <el-radio :label="3">高</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="影响范围" prop="impactScope">
          <el-radio-group v-model="form.impactScope">
            <el-radio :label="1">个人</el-radio>
            <el-radio :label="2">部门</el-radio>
            <el-radio :label="3">全公司</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="问题描述" prop="description">
          <div ref="editorRef" class="editor-container"></div>
        </el-form-item>

        <el-form-item label="附件">
          <el-upload
            :action="uploadUrl"
            :file-list="fileList"
            :limit="5"
            :accept="acceptTypes"
            :before-upload="beforeUpload"
            :on-success="onUploadSuccess"
            :on-error="onUploadError"
            :on-remove="onRemoveFile"
            list-type="picture-card"
          >
            <el-icon size="24"><Plus /></el-icon>
          </el-upload>
          <span class="upload-tip">支持jpg、png、pdf、docx、xlsx格式，单文件最大10MB</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="btn-primary-gradient" :loading="loading" @click="handleSubmit">
            提交工单
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { ticketApi, adminApi } from '@/api';
import { ElMessage } from 'element-plus';
import E from 'wangeditor';
const router = useRouter();
const loading = ref(false);
const formRef = ref(null);
const editorRef = ref(null);
let editor = null;
const uploadUrl = '/api/tickets/upload';
const acceptTypes = '.jpg,.png,.pdf,.docx,.xlsx';
const form = reactive({
 title: '',
 typeId: '',
 urgency: 1,
 impactScope: 1,
 description: ''
});
const rules = {
 title: [
 { required: true, message: '请输入工单标题', trigger: 'blur' },
 { max: 200, message: '标题长度不能超过200个字符', trigger: 'blur' }
 ],
 typeId: [
 { required: true, message: '请选择工单类型', trigger: 'change' }
 ],
 urgency: [
 { required: true, message: '请选择紧急程度', trigger: 'change' }
 ],
 impactScope: [
 { required: true, message: '请选择影响范围', trigger: 'change' }
 ]
};
const ticketTypes = ref([]);
const fileList = ref([]);
const handleSubmit = async () => {
 if (!formRef.value)
 return;
 formRef.value.validate(async (valid) => {
 if (!valid)
 return;
 form.description = editor.getText();
 loading.value = true;
 try {
 const data = {
 title: form.title,
 typeId: form.typeId,
 urgency: form.urgency,
 impactScope: form.impactScope,
 description: form.description,
 attachmentIds: fileList.value.map(f => f.id)
 };
 await ticketApi.create(data);
 ElMessage.success('工单创建成功');
 router.push('/tickets');
 }
 catch (error) {
 ElMessage.error('创建失败: ' + error.message);
 }
 finally {
 loading.value = false;
 }
 });
};
const goBack = () => {
 router.back();
};
const beforeUpload = (file) => {
 const maxSize = 10 * 1024 * 1024;
 if (file.size > maxSize) {
 ElMessage.error('文件大小不能超过10MB');
 return false;
 }
 return true;
};
const onUploadSuccess = (response) => {
 if (response.code === 200) {
 fileList.value.push({
 id: response.data.id,
 name: response.data.fileName,
 url: response.data.filePath,
 status: 'success'
 });
 }
 else {
 ElMessage.error('上传失败: ' + response.msg);
 }
};
const onUploadError = () => {
 ElMessage.error('上传失败');
};
const onRemoveFile = (file) => {
 const index = fileList.value.findIndex(f => f.name === file.name);
 if (index > -1) {
 fileList.value.splice(index, 1);
 }
};
const fetchTicketTypes = async () => {
 try {
 ticketTypes.value = await adminApi.roles();
 }
 catch (error) {
 console.error('获取工单类型失败');
 }
};
onMounted(() => {
 fetchTicketTypes();
 if (editorRef.value) {
 editor = new E(editorRef.value);
 editor.config.height = 200;
 editor.config.uploadImgServer = '/api/tickets/upload';
 editor.config.uploadImgMaxSize = 10 * 1024 * 1024;
 editor.config.uploadImgAccept = ['jpg', 'jpeg', 'png'];
 editor.create();
 }
});
onUnmounted(() => {
 if (editor) {
 editor.destroy();
 }
});
</script>

<style scoped>
.editor-container {
  border: 1px solid #ebf0f5;
  border-radius: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.back-btn {
  margin-left: auto;
}
</style>