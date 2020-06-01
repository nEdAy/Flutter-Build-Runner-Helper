package cn.neday.excavator.checker

interface ICheck {
    /**
     * 校验方法
     *
     * @param path 待检查路径
     * @return 校验结果，true 为 通过
     */
    fun check(path: String?): Boolean
}