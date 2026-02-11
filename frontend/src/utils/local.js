import {ElMessage} from "element-plus";

const CopyToClipboard = async (text) => {

    try {
        if (typeof navigator !== 'undefined' && navigator.clipboard) {
            // 现代浏览器使用 Clipboard API
            await navigator.clipboard.writeText(text || '');
        } else {
            // 降级处理：使用 execCommand
            const textarea = document.createElement('textarea');
            textarea.value = text || '';
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
        }
        /*alert('已复制到剪贴板！');*/
        ElMessage({
            type: 'success',
            message: `已复制到剪贴板！`,
        })
    } catch (err) {
        console.error('复制失败:', err);
        ElMessage({
            type: 'error',
            message: `复制失败，请手动复制内容。`,
        });
    }
};

export {
    CopyToClipboard
}