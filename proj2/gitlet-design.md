# Gitlet Design Document

**Name**: 22-debug

## Classes and Data Structures

### Blob

#### 保存文件

1. 保存单个文件的内容
2. 用哈希当名字
3. 存在 .gitlet/blobs/


### Commit

#### 保存版本

1. 保存：
   - 时间戳
   - 作者信息
   - 父Commit
   - 文件路径--blob哈希的映射
2. 用哈希当名字
3. 存在 .gitlet/commits/

### Stage

#### 暂存区

1. 两个map：
   1. added：要加入的文件
   2. removed：要删除的文件
2. 存在 .gitlet/stage/

### HEAD/Branch

#### 存指针

1. HEAD指向当前分支
2. 每个分支指向最新的commit
3. 存在 .gitlet/heads/

## Algorithms

## Persistence

