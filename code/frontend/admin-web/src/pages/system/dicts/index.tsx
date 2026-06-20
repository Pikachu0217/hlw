import {
  CloseOutlined,
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons';
import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Key } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { createDict, deleteDict, fetchDicts, updateDict } from '@/api/modules';
import { useModuleRecords } from '@/hooks/useModuleRecords';

const DEFAULT_DICT_SORT = 0;
const DICT_TYPE_CSV_FILE_NAME = 'dict-type.csv';
const DICT_DATA_CSV_FILE_NAME = 'dict-data.csv';
const DICT_DATA_VIEW = 'data';
const DEFAULT_STATUS_TEXT = '正常';
const EMPTY_TIME_TEXT = '-';

export interface DictRecord {
  /** 字典项编号。 */
  id: number;
  /** 字典名称。 */
  dictName?: string;
  /** 字典类型编码。 */
  dictType: string;
  /** 字典标签。 */
  dictLabel: string;
  /** 字典键值。 */
  dictValue: string;
  /** 字典排序。 */
  dictSort?: number;
  /** 字典备注。 */
  remark?: string;
}

interface DictTypeGroup {
  /** 字典类型主记录编号。 */
  firstRecordId: number;
  /** 字典类型编码。 */
  dictType: string;
  /** 字典名称。 */
  dictName: string;
  /** 字典备注。 */
  remark?: string;
  /** 字典项数量。 */
  count: number;
}

function DictsPage() {
  const { records, loading, refresh } = useModuleRecords(fetchDicts, '字典');
  const [searchParams, setSearchParams] = useSearchParams();
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<DictRecord | null>(null);
  const [typeNameKeyword, setTypeNameKeyword] = useState('');
  const [typeCodeKeyword, setTypeCodeKeyword] = useState('');
  const [dataKeyword, setDataKeyword] = useState('');
  const [selectedTypeKeys, setSelectedTypeKeys] = useState<Key[]>([]);
  const [selectedDataIds, setSelectedDataIds] = useState<Key[]>([]);
  const activeView = searchParams.get('view') === DICT_DATA_VIEW ? DICT_DATA_VIEW : 'type';
  const activeDictType = searchParams.get('dictType') ?? '';

  const dictTypeGroups = useMemo<DictTypeGroup[]>(() => buildDictTypeGroups(records), [records]);
  const filteredTypeGroups = useMemo(
    () => filterDictTypeGroups(dictTypeGroups, typeNameKeyword, typeCodeKeyword),
    [dictTypeGroups, typeNameKeyword, typeCodeKeyword],
  );
  const selectedTypeGroup = dictTypeGroups.find((group) => group.dictType === selectedTypeKeys[0]);
  const activeGroup = dictTypeGroups.find((group) => group.dictType === activeDictType);
  const selectedRecords = useMemo(
    () => filterDictRecords(records, activeDictType, dataKeyword),
    [records, activeDictType, dataKeyword],
  );
  const selectedRecord = selectedDataIds.length === 1
    ? selectedRecords.find((record) => record.id === selectedDataIds[0])
    : undefined;

  useEffect(() => {
    setSelectedDataIds([]);
  }, [activeDictType]);

  /** 聚合字典类型数据，供字典管理主列表展示。 */
  function buildDictTypeGroups(dictRecords: DictRecord[]): DictTypeGroup[] {
    const groupMap = new Map<string, DictTypeGroup>();

    dictRecords.forEach((record) => {
      const existed = groupMap.get(record.dictType);
      if (existed) {
        existed.count += 1;
        if (!existed.remark && record.remark) {
          existed.remark = record.remark;
        }
        return;
      }

      groupMap.set(record.dictType, {
        firstRecordId: record.id,
        dictType: record.dictType,
        dictName: record.dictName || record.dictType,
        remark: record.remark,
        count: 1,
      });
    });

    return Array.from(groupMap.values()).sort((left, right) => left.dictType.localeCompare(right.dictType));
  }

  /** 根据字典名称和字典类型过滤主列表。 */
  function filterDictTypeGroups(groups: DictTypeGroup[], nameKeyword: string, codeKeyword: string): DictTypeGroup[] {
    const normalizedNameKeyword = nameKeyword.trim().toLowerCase();
    const normalizedCodeKeyword = codeKeyword.trim().toLowerCase();

    return groups.filter((group) => {
      const matchName = normalizedNameKeyword
        ? `${group.dictName} ${group.remark ?? ''}`.toLowerCase().includes(normalizedNameKeyword)
        : true;
      const matchCode = normalizedCodeKeyword ? group.dictType.toLowerCase().includes(normalizedCodeKeyword) : true;
      return matchName && matchCode;
    });
  }

  /** 根据当前字典类型和字典标签过滤数据列表。 */
  function filterDictRecords(dictRecords: DictRecord[], dictType: string, keyword: string): DictRecord[] {
    const normalizedKeyword = keyword.trim().toLowerCase();
    return dictRecords
      .filter((record) => (dictType ? record.dictType === dictType : true))
      .filter((record) => {
        if (!normalizedKeyword) {
          return true;
        }
        return `${record.dictLabel} ${record.dictValue} ${record.remark ?? ''}`.toLowerCase().includes(normalizedKeyword);
      });
  }

  /** 跳转到字典数据列表，并带入当前字典类型。 */
  function handleOpenDataList(group: DictTypeGroup): void {
    setSearchParams({ view: DICT_DATA_VIEW, dictType: group.dictType });
  }

  /** 返回字典管理主列表。 */
  function handleCloseDataList(): void {
    setSearchParams({});
  }

  /** 打开新增字典项弹窗。 */
  function handleOpenCreate(group?: DictTypeGroup): void {
    const seedGroup = group ?? (activeView === DICT_DATA_VIEW ? activeGroup : undefined);

    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({
      dictName: seedGroup?.dictName,
      dictType: seedGroup?.dictType,
      dictSort: DEFAULT_DICT_SORT,
    });
    setOpen(true);
  }

  /** 打开编辑弹窗并回填当前字典项。 */
  function handleOpenEdit(record: DictRecord): void {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setOpen(true);
  }

  /** 打开字典类型编辑弹窗，使用该类型首条字典数据作为承载记录。 */
  function handleOpenTypeEdit(group?: DictTypeGroup): void {
    const targetGroup = group ?? selectedTypeGroup;
    const targetRecord = records.find((record) => record.id === targetGroup?.firstRecordId);

    if (!targetRecord) {
      message.info('请选择要修改的字典');
      return;
    }
    handleOpenEdit(targetRecord);
  }

  /** 提交新增或编辑后的字典项数据。 */
  async function handleSubmit(): Promise<void> {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateDict(editingRecord.id, values);
        message.success('字典项更新成功');
      } else {
        await createDict(values);
        message.success('字典项创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
      if (activeView === DICT_DATA_VIEW) {
        setSearchParams({ view: DICT_DATA_VIEW, dictType: values.dictType });
      }
    } catch {
      message.warning(editingRecord ? '字典项更新失败，请检查接口或稍后重试' : '字典项创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  /** 删除指定字典项。 */
  function handleDelete(record: DictRecord): void {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除字典项"${record.dictLabel}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteDict(record.id);
          message.success('字典项删除成功');
          refresh();
        } catch {
          message.warning('字典项删除失败，请稍后重试');
        }
      },
    });
  }

  /** 删除指定字典类型下的全部字典项。 */
  function handleDeleteGroup(group?: DictTypeGroup): void {
    const targetGroup = group ?? selectedTypeGroup;
    const targetRecords = records.filter((record) => record.dictType === targetGroup?.dictType);

    if (!targetGroup || targetRecords.length === 0) {
      message.info('请选择要删除的字典');
      return;
    }

    Modal.confirm({
      title: '确认删除',
      content: `确定要删除字典"${targetGroup.dictName}"下的 ${targetRecords.length} 条数据吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await Promise.all(targetRecords.map((record) => deleteDict(record.id)));
          message.success('字典删除成功');
          setSelectedTypeKeys([]);
          refresh();
        } catch {
          message.warning('字典删除失败，请稍后重试');
        }
      },
    });
  }

  /** 导出 CSV 文件。 */
  function exportCsv(fileName: string, csvRows: string[][]): void {
    const csvContent = csvRows.map((row) => row.map((cell) => `"${cell.replace(/"/g, '""')}"`).join(',')).join('\n');
    const blob = new Blob([`\uFEFF${csvContent}`], { type: 'text/csv;charset=utf-8;' });
    const downloadUrl = URL.createObjectURL(blob);
    const anchor = document.createElement('a');

    anchor.href = downloadUrl;
    anchor.download = fileName;
    anchor.click();
    URL.revokeObjectURL(downloadUrl);
  }

  /** 导出字典管理主列表。 */
  function handleExportTypes(): void {
    if (filteredTypeGroups.length === 0) {
      message.info('当前没有可导出的字典');
      return;
    }

    exportCsv(DICT_TYPE_CSV_FILE_NAME, [
      ['字典名称', '字典类型', '状态', '备注'],
      ...filteredTypeGroups.map((group) => [
        group.dictName,
        group.dictType,
        DEFAULT_STATUS_TEXT,
        group.remark ?? `${group.dictName}列表`,
      ]),
    ]);
  }

  /** 导出当前字典数据列表。 */
  function handleExportData(): void {
    if (selectedRecords.length === 0) {
      message.info('当前没有可导出的字典数据');
      return;
    }

    exportCsv(DICT_DATA_CSV_FILE_NAME, [
      ['字典标签', '字典键值', '字典排序', '备注'],
      ...selectedRecords.map((record) => [
        record.dictLabel,
        record.dictValue,
        String(record.dictSort ?? ''),
        record.remark ?? '',
      ]),
    ]);
  }

  const typeColumns = useMemo<ColumnsType<DictTypeGroup>>(
    () => [
      { title: '字典编号', width: 110, render: (_: unknown, __: DictTypeGroup, index: number) => index + 1 },
      { title: '字典名称', dataIndex: 'dictName', width: 180 },
      {
        title: '字典类型',
        dataIndex: 'dictType',
        width: 220,
        render: (value: string) => <Typography.Text className="dict-type-code">{value}</Typography.Text>,
      },
      {
        title: '状态',
        width: 120,
        render: () => <Tag color="blue" className="dict-status-tag">{DEFAULT_STATUS_TEXT}</Tag>,
      },
      { title: '备注', dataIndex: 'remark', width: 220, render: (value: string | undefined, group: DictTypeGroup) => value || `${group.dictName}列表` },
      { title: '创建时间', width: 180, render: () => EMPTY_TIME_TEXT },
      {
        title: '操作',
        key: 'actions',
        width: 210,
        render: (_: unknown, group: DictTypeGroup) => (
          <Space size="small">
            <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleOpenTypeEdit(group)}>
              修改
            </Button>
            <Button type="link" size="small" icon={<UnorderedListOutlined />} onClick={() => handleOpenDataList(group)}>
              列表
            </Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteGroup(group)}>
              删除
            </Button>
          </Space>
        ),
      },
    ],
    [records, selectedTypeGroup],
  );

  const dataColumns = useMemo<ColumnsType<DictRecord>>(
    () => [
      { title: '字典编码', width: 110, render: (_: unknown, __: DictRecord, index: number) => index + 1 },
      { title: '字典标签', dataIndex: 'dictLabel', width: 180 },
      { title: '字典键值', dataIndex: 'dictValue', width: 180 },
      { title: '字典排序', dataIndex: 'dictSort', width: 150 },
      {
        title: '状态',
        width: 120,
        render: () => <Tag color="blue" className="dict-status-tag">{DEFAULT_STATUS_TEXT}</Tag>,
      },
      { title: '备注', dataIndex: 'remark', width: 220 },
      { title: '创建时间', width: 180, render: () => EMPTY_TIME_TEXT },
      {
        title: '操作',
        key: 'actions',
        width: 150,
        render: (_: unknown, record: DictRecord) => (
          <Space size="small">
            <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleOpenEdit(record)}>
              修改
            </Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
              删除
            </Button>
          </Space>
        ),
      },
    ],
    [],
  );

  return (
    <>
      <div className="dict-list-page">
        <div className="dict-view-tabs">
          <Button type={activeView === 'type' ? 'primary' : 'default'} onClick={handleCloseDataList}>
            字典管理
          </Button>
          {activeView === DICT_DATA_VIEW ? (
            <Button type="primary">
              字典数据
            </Button>
          ) : null}
        </div>
        {activeView === 'type' ? (
          <>
            <div className="dict-search-grid dict-search-grid--type">
              <label className="dict-search-item">
                <span>字典名称</span>
                <Input
                  allowClear
                  value={typeNameKeyword}
                  onChange={(event) => setTypeNameKeyword(event.target.value)}
                  placeholder="请输入字典名称"
                />
              </label>
              <label className="dict-search-item">
                <span>字典类型</span>
                <Input
                  allowClear
                  value={typeCodeKeyword}
                  onChange={(event) => setTypeCodeKeyword(event.target.value)}
                  placeholder="请输入字典类型"
                />
              </label>
              <label className="dict-search-item">
                <span>状态</span>
                <Select allowClear placeholder="字典状态" options={[{ label: DEFAULT_STATUS_TEXT, value: DEFAULT_STATUS_TEXT }]} />
              </label>
              <div className="dict-search-actions">
                <Button type="primary" icon={<SearchOutlined />}>
                  搜索
                </Button>
                <Button icon={<ReloadOutlined />} onClick={() => {
                  setTypeNameKeyword('');
                  setTypeCodeKeyword('');
                }}>
                  重置
                </Button>
              </div>
            </div>
            <div className="dict-list-toolbar">
              <Space wrap>
                <Button type="primary" ghost icon={<PlusOutlined />} onClick={() => handleOpenCreate()}>
                  新增
                </Button>
                <Button icon={<EditOutlined />} disabled={!selectedTypeGroup} onClick={() => handleOpenTypeEdit()}>
                  修改
                </Button>
                <Button danger icon={<DeleteOutlined />} disabled={!selectedTypeGroup} onClick={() => handleDeleteGroup()}>
                  删除
                </Button>
                <Button icon={<DownloadOutlined />} onClick={handleExportTypes}>
                  导出
                </Button>
                <Button danger ghost icon={<ReloadOutlined />} onClick={refresh}>
                  刷新缓存
                </Button>
              </Space>
              <Space>
                <Button shape="circle" icon={<SearchOutlined />} aria-label="搜索字典" />
                <Button shape="circle" icon={<ReloadOutlined />} aria-label="刷新字典" onClick={refresh} />
              </Space>
            </div>
            <Table<DictTypeGroup>
              rowKey="dictType"
              className="system-compact-table dict-plain-table"
              columns={typeColumns}
              dataSource={filteredTypeGroups}
              loading={loading}
              pagination={{ pageSize: 10, showSizeChanger: false }}
              rowSelection={{
                selectedRowKeys: selectedTypeKeys,
                onChange: setSelectedTypeKeys,
              }}
              onRow={(record) => ({
                onClick: () => setSelectedTypeKeys([record.dictType]),
              })}
            />
          </>
        ) : (
          <>
            <div className="dict-search-grid dict-search-grid--data">
              <label className="dict-search-item">
                <span>字典名称</span>
                <Select
                  allowClear
                  value={activeDictType || undefined}
                  placeholder="请选择字典名称"
                  onChange={(value) => {
                    if (value) {
                      setSearchParams({ view: DICT_DATA_VIEW, dictType: value });
                      return;
                    }
                    setSearchParams({ view: DICT_DATA_VIEW });
                  }}
                  onClear={() => setSearchParams({ view: DICT_DATA_VIEW })}
                  options={dictTypeGroups.map((group) => ({ label: group.dictName, value: group.dictType }))}
                />
              </label>
              <label className="dict-search-item">
                <span>字典标签</span>
                <Input
                  allowClear
                  value={dataKeyword}
                  onChange={(event) => setDataKeyword(event.target.value)}
                  placeholder="请输入字典标签"
                />
              </label>
              <label className="dict-search-item">
                <span>状态</span>
                <Select allowClear placeholder="数据状态" options={[{ label: DEFAULT_STATUS_TEXT, value: DEFAULT_STATUS_TEXT }]} />
              </label>
              <div className="dict-search-actions">
                <Button type="primary" icon={<SearchOutlined />}>
                  搜索
                </Button>
                <Button icon={<ReloadOutlined />} onClick={() => setDataKeyword('')}>
                  重置
                </Button>
              </div>
            </div>
            <div className="dict-list-toolbar">
              <Space wrap>
                <Button type="primary" ghost icon={<PlusOutlined />} onClick={() => handleOpenCreate(activeGroup)}>
                  新增
                </Button>
                <Button icon={<EditOutlined />} disabled={!selectedRecord} onClick={() => selectedRecord && handleOpenEdit(selectedRecord)}>
                  修改
                </Button>
                <Button danger icon={<DeleteOutlined />} disabled={!selectedRecord} onClick={() => selectedRecord && handleDelete(selectedRecord)}>
                  删除
                </Button>
                <Button icon={<DownloadOutlined />} onClick={handleExportData}>
                  导出
                </Button>
                <Button icon={<CloseOutlined />} onClick={handleCloseDataList}>
                  关闭
                </Button>
              </Space>
              <Space>
                <Button shape="circle" icon={<SearchOutlined />} aria-label="搜索字典数据" />
                <Button shape="circle" icon={<ReloadOutlined />} aria-label="刷新字典数据" onClick={refresh} />
              </Space>
            </div>
            <Table<DictRecord>
              rowKey="id"
              className="system-compact-table dict-plain-table"
              columns={dataColumns}
              dataSource={selectedRecords}
              loading={loading}
              pagination={{ pageSize: 10, showSizeChanger: false }}
              rowSelection={{
                selectedRowKeys: selectedDataIds,
                onChange: setSelectedDataIds,
              }}
            />
          </>
        )}
      </div>
      <Modal
        title={editingRecord ? '编辑字典项' : '新增字典项'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="dictName" label="字典名称">
            <Input placeholder="请输入字典名称" />
          </Form.Item>
          <Form.Item name="dictType" label="字典类型" rules={[{ required: true, message: '请输入字典类型' }]}>
            <Input placeholder="例如：sys_normal_disable" />
          </Form.Item>
          <Form.Item name="dictLabel" label="字典标签" rules={[{ required: true, message: '请输入字典标签' }]}>
            <Input placeholder="请输入字典标签" />
          </Form.Item>
          <Form.Item name="dictValue" label="字典键值" rules={[{ required: true, message: '请输入字典键值' }]}>
            <Input placeholder="请输入字典键值" />
          </Form.Item>
          <Form.Item name="dictSort" label="排序">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default DictsPage;
