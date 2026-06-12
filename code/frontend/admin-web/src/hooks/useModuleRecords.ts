import { message } from 'antd';
import { useEffect, useState } from 'react';

// 加载管理端模块列表数据，并统一处理加载态和错误提示。
export function useModuleRecords<T>(loader: () => Promise<T[]>, moduleName: string) {
  const [records, setRecords] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    loader()
      .then((items) => {
        if (!ignore) {
          setRecords(items);
        }
      })
      .catch(() => {
        message.warning(`${moduleName}接口暂不可用，请稍后重试`);
      })
      .finally(() => {
        if (!ignore) {
          setLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, [loader, moduleName]);

  return { records, loading };
}
