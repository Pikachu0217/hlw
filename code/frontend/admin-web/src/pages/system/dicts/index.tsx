import {
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import { Button, Card, Form, Input, InputNumber, Modal, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Key } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { createDict, deleteDict, fetchDicts, updateDict } from '@/api/modules';
import { useModuleRecords } from '@/hooks/useModuleRecords';

const DEFAULT_DICT_SORT = 0;
const DICT_TYPE_TABLE_SCROLL_WIDTH = 680;
const DICT_DATA_TABLE_SCROLL_WIDTH = 760;
const CSV_FILE_NAME = 'dict-data.csv';

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
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<DictRecord | null>(null);
  const [typeKeyword, setTypeKeyword] = useState('');
  const [dataKeyword, setDataKeyword] = useState('');
  const [selectedDictType, setSelectedDictType] = useState<string>('');
  const [selectedDataIds, setSelectedDataIds] = useState<Key[]>([]);

  const dictTypeGroups = useMemo<DictTypeGroup[]>(() => buildDictTypeGroups(records), [records]);
  const filteredTypeGroups = useMemo(
    () => filterDictTypeGroups(dictTypeGroups, typeKeyword),
    [dictTypeGroups, typeKeyword],
  );
  const selectedGroup = dictTypeGroups.find((group) => group.dictType === selectedDictType);
  const selectedRecords = useMemo(
    () => filterDictRecords(records, selectedGroup?.dictType ?? '', dataKeyword),
    [records, selectedGroup?.dictType, dataKeyword],
  );
  const selectedRecord = selectedDataIds.length === 1
    ? selectedRecords.find((record) => record.id === selectedDataIds[0])
    : undefined;

  useEffect(() => {
    if (filteredTypeGroups.length === 0) {
      setSelectedDictType('');
      return;
    }

    if (!filteredTypeGroups.some((group) => group.dictType === selectedDictType)) {
      setSelectedDictType(filteredTypeGroups[0].dictType);
    }
  }, [filteredTypeGroups, selectedDictType]);

  useEffect(() => {
    setSelectedDataIds([]);
  }, [selectedGroup?.dictType]);

  /** 聚合字典类型数据，供左侧字典管理列表展示。 */
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
        dictType: record.dictType,
        dictName: record.dictName || record.dictType,
        remark: record.remark,
        count: 1,
      });
    });

    return Array.from(groupMap.values()).sort((left, right) => left.dictType.localeCompare(right.dictType));
  }

  /** 根据字典名称和类型编码过滤左侧字典类型。 */
  function filterDictTypeGroups(groups: DictTypeGroup[], keyword: string): DictTypeGroup[] {
    const normalizedKeyword = keyword.trim().toLowerCase();
    if (!normalizedKeyword) {
      return groups;
    }

    return groups.filter((group) => `${group.dictName} ${group.dictType} ${group.remark ?? ''}`.toLowerCase().includes(normalizedKeyword));
  }

  /** 根据当前选中字典类型和搜索词过滤右侧字典项。 */
  function filterDictRecords(dictRecords: DictRecord[], dictType: string, keyword: string): DictRecord[] {
    const normalizedKeyword = keyword.trim().toLowerCase();
    return dictRecords
      .filter((record) => record.dictType === dictType)
      .filter((record) => {
        if (!normalizedKeyword) {
          return true;
        }
        return `${record.dictLabel} ${record.dictValue} ${record.remark ?? ''}`.toLowerCase().includes(normalizedKeyword);
      });
  }

  /** 打开新增字典项弹窗，并带入当前选中的字典类型。 */
  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({
      dictName: selectedGroup?.dictName,
      dictType: selectedGroup?.dictType,
      dictSort: DEFAULT_DICT_SORT,
    });
    setOpen(true);
  };

  /** 打开编辑弹窗并回填当前字典项。 */
  const handleOpenEdit = (record: DictRecord) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setOpen(true);
  };

  /** 提交新增或编辑后的字典项数据。 */
  const handleSubmit = async () => {
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
      setSelectedDictType(values.dictType);
    } catch {
      message.warning(editingRecord ? '字典项更新失败，请检查接口或稍后重试' : '字典项创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  /** 删除指定字典项。 */
  const handleDelete = (record: DictRecord) => {
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
  };

  /** 导出当前选中字典类型下的字典项。 */
  const handleExport = () => {
    if (selectedRecords.length === 0) {
      message.info('当前没有可导出的字典数据');
      return;
    }

    const csvRows = [
      ['字典标签', '字典键值', '字典排序', '备注'],
      ...selectedRecords.map((record) => [
        record.dictLabel,
        record.dictValue,
        String(record.dictSort ?? ''),
        record.remark ?? '',
      ]),
    ];
    const csvContent = csvRows.map((row) => row.map((cell) => `"${cell.replace(/"/g, '""')}"`).join(',')).join('\n');
    const blob = new Blob([`\uFEFF${csvContent}`], { type: 'text/csv;charset=utf-8;' });
    const downloadUrl = URL.createObjectURL(blob);
    const anchor = document.createElement('a');

    anchor.href = downloadUrl;
    anchor.download = CSV_FILE_NAME;
    anchor.click();
    URL.revokeObjectURL(downloadUrl);
  };

  const typeColumns = useMemo<ColumnsType<DictTypeGroup>>(
    () => [
      { title: '字典名称', dataIndex: 'dictName', width: 160 },
      {
        title: '字典类型',
        dataIndex: 'dictType',
        width: 220,
        render: (value: string) => <Typography.Text className="dict-type-code">{value}</Typography.Text>,
      },
      { title: '备注', dataIndex: 'remark', width: 180, render: (value?: string) => value || '-' },
      {
        title: '操作',
        key: 'actions',
        width: 110,
        render: (_: unknown, group: DictTypeGroup) => (
          <Button
            type="link"
            size="small"
            icon={<PlusOutlined />}
            onClick={(event) => {
              event.stopPropagation();
              setSelectedDictType(group.dictType);
              form.resetFields();
              form.setFieldsValue({
                dictName: group.dictName,
                dictType: group.dictType,
                dictSort: DEFAULT_DICT_SORT,
              });
              setEditingRecord(null);
              setOpen(true);
            }}
          >
            添加
          </Button>
        ),
      },
    ],
    [form],
  );

  const dataColumns = useMemo<ColumnsType<DictRecord>>(
    () => [
      { title: '字典标签', dataIndex: 'dictLabel' },
      { title: '字典键值', dataIndex: 'dictValue' },
      { title: '字典排序', dataIndex: 'dictSort' },
      { title: '备注', dataIndex: 'remark' },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: DictRecord) => (
          <Space size="small">
            <Button type="link" size="small" onClick={() => handleOpenEdit(record)}>
              编辑
            </Button>
            <Button type="link" size="small" danger onClick={() => handleDelete(record)}>
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
      <div className="dict-workspace">
        <Card className="dict-panel dict-panel--type" bordered={false}>
          <div className="dict-panel__header">
            <Typography.Title level={3} className="dict-panel__title">
              字典管理
            </Typography.Title>
            <Space>
              <Button shape="circle" icon={<SearchOutlined />} aria-label="搜索字典" />
              <Button shape="circle" icon={<ReloadOutlined />} aria-label="刷新字典" onClick={refresh} />
            </Space>
          </div>
          <div className="dict-panel__body">
            <Form layout="vertical" className="dict-filter-form">
              <Form.Item label="字典名称">
                <Input
                  allowClear
                  value={typeKeyword}
                  onChange={(event) => setTypeKeyword(event.target.value)}
                  placeholder="请输入字典名称或类型"
                />
              </Form.Item>
              <Space wrap className="dict-toolbar">
                <Button type="primary" ghost icon={<PlusOutlined />} onClick={handleOpenCreate}>
                  新增
                </Button>
                <Button icon={<EditOutlined />} disabled>
                  修改
                </Button>
                <Button danger icon={<DeleteOutlined />} disabled>
                  删除
                </Button>
                <Button icon={<DownloadOutlined />} onClick={handleExport}>
                  导出
                </Button>
                <Button type="primary" icon={<SearchOutlined />}>
                  搜索
                </Button>
                <Button icon={<ReloadOutlined />} onClick={() => setTypeKeyword('')}>
                  重置
                </Button>
              </Space>
            </Form>
            <Table<DictTypeGroup>
              rowKey="dictType"
              className="dict-type-table"
              columns={typeColumns}
              dataSource={filteredTypeGroups}
              loading={loading}
              pagination={false}
              scroll={{ x: DICT_TYPE_TABLE_SCROLL_WIDTH }}
              rowSelection={{
                type: 'radio',
                selectedRowKeys: selectedGroup ? [selectedGroup.dictType] : [],
                onChange: (keys) => setSelectedDictType(String(keys[0] ?? '')),
              }}
              onRow={(record) => ({
                onClick: () => setSelectedDictType(record.dictType),
              })}
            />
          </div>
        </Card>
        <Card className="dict-panel dict-panel--data" bordered={false}>
          <div className="dict-panel__header">
            <div className="dict-panel__title-wrap">
              <Typography.Title level={3} className="dict-panel__title">
                字典数据
              </Typography.Title>
              <Typography.Text className="dict-panel__crumb">
                {selectedGroup ? `${selectedGroup.dictName} / ${selectedGroup.dictType}` : '请选择字典'}
              </Typography.Text>
            </div>
            <Space>
              <Button shape="circle" icon={<SearchOutlined />} aria-label="搜索字典数据" />
              <Button shape="circle" icon={<ReloadOutlined />} aria-label="刷新字典数据" onClick={refresh} />
            </Space>
          </div>
          <div className="dict-panel__body">
            <div className="dict-data-filter">
              <Typography.Text className="dict-filter-label" strong>
                字典标签
              </Typography.Text>
              <Input
                allowClear
                value={dataKeyword}
                onChange={(event) => setDataKeyword(event.target.value)}
                placeholder="请输入字典标签"
                className="dict-data-filter__input"
              />
              <Button type="primary" icon={<SearchOutlined />}>
                搜索
              </Button>
              <Button icon={<ReloadOutlined />} onClick={() => setDataKeyword('')}>
                重置
              </Button>
            </div>
            <Space wrap className="dict-toolbar">
              <Button type="primary" ghost icon={<PlusOutlined />} onClick={handleOpenCreate} disabled={!selectedGroup}>
                新增
              </Button>
              <Button icon={<EditOutlined />} disabled={!selectedRecord} onClick={() => selectedRecord && handleOpenEdit(selectedRecord)}>
                修改
              </Button>
              <Button danger icon={<DeleteOutlined />} disabled={!selectedRecord} onClick={() => selectedRecord && handleDelete(selectedRecord)}>
                删除
              </Button>
              <Button icon={<DownloadOutlined />} onClick={handleExport}>
                导出
              </Button>
              <Tag color="blue">共 {selectedRecords.length} 条</Tag>
            </Space>
            <Table<DictRecord>
              rowKey="id"
              className="dict-data-table"
              columns={dataColumns}
              dataSource={selectedRecords}
              loading={loading}
              pagination={{ pageSize: 10, showSizeChanger: false }}
              scroll={{ x: DICT_DATA_TABLE_SCROLL_WIDTH }}
              rowSelection={{
                selectedRowKeys: selectedDataIds,
                onChange: setSelectedDataIds,
              }}
            />
          </div>
        </Card>
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
