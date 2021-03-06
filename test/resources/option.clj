;; 联合体 Option

;; union Option
;;     Some(Int value)
;;     None
;; end
;;
;; 函数：
;; Option::Some(Int value) -> Option::Some
;;
;; 常量：
;; Option::None


;; !! 模块名称 "std"

(namespace Option
    ;; 私有方法
    ;; std::Option::new(WordWidth member_type_index, Any member_addr) -> std::Option
    ;; 构建联合体的结构体类型成员

    (defn new
        (member_type_index member_addr)
        (do
            (let addr (builtin.memory.create_struct 16 2))
            (builtin.memory.write_i64 addr 0 member_type_index)
            (builtin.memory.add_ref addr 8 member_addr)
            addr
        )
    )

    ;; 私有方法
    ;; std::Option::new$1(WordWidth member_type_index) -> std::Option
    ;; 构建联合体的常量型成员

    (defn new$1
        (member_type_index)
        (do
            (let addr (builtin.memory.create_struct 16 0))
            (builtin.memory.write_i64 addr 0 member_type_index) ;;!注意必须把空的字段填上 0，JavaScript 会截断空字段
            (builtin.memory.write_i64 addr 8 0)
            addr
        )
    )

    ;; std::Option::Some(Int value) -> Option::Some
    ;; 快捷构建子成员的方法

    (defn Some (value)
        (do
            (let addr (std.Option.Some.new value))
            (new 0 addr)
        )
    )

    ;; std::Option::None

    (const None
        (do
            (let addr (new$1 1))
            (builtin.memory.inc_ref addr) ;; const 需要增加引用值
            addr
        )
    )

    ;; std::Option::getMemberTypeIndex(Option) -> i64
    ;; 内部方法，获取当前联合体的值的子类型索引
    (defn getMemberTypeIndex
        (addr)
        (builtin.memory.read_i64 addr 0)
    )

    ;; std::Option::getMember(Option) -> Any
    ;; 内部方法，获取当前联合体的值（某个从属结构体的实例/地址）
    ;; 联合体的值是其子成员的其中之一
    ;; 如果某个子成员是常量类型，则抛出异常
    (defn getMember
        (addr)
        (do
            (let member_type_index (getMemberTypeIndex addr))

            (if (native.i64.eq member_type_index 0) ;; 0 号子成员是从属结构体
                (builtin.memory.read_address addr 8)
                (if (native.i64.eq member_type_index 1) ;; 1 号子成员是常量型，无从属结构体
                    (builtin.panic 10003) ;; 无从属结构体
                    (builtin.panic 10001) ;; 非联合体成员
                )
            )
        )
    )

    ;; std::Option::equal(Option left, Option right) -> i64

    (defn equal
        (left_addr right_addr)
        (do
            (let left_member_type_index (builtin.memory.read_i64 left_addr 0))
            (let right_member_type_index (builtin.memory.read_i64 right_addr 0))

                (if (native.i64.eq left_member_type_index right_member_type_index)
                    (if (native.i64.eq left_member_type_index 0)
                        ;; Option::Some
                        (std.Option.Some.equal
                            (builtin.memory.read_address left_addr 8)
                            (builtin.memory.read_address right_addr 8)
                        )
                        ;; Option::None
                        (if (native.i64.eq left_member_type_index 1)
                            1

                            ;; 非联合体成员
                            (builtin.panic 10001)
                        )
                    )
                    0
                )
        )
    )
)

(namespace Option.Some

    ;; 私有方法
    ;; std::Option::Some::new(Int value) -> std::Option::Some

    (defn new
        (value)
        (do
            (let addr (builtin.memory.create_struct 8 0))
            (builtin.memory.write_i64 addr 0 value)
            addr
        )
    )

    (defn getValue
        (addr)
        (builtin.memory.read_i64 addr 0)
    )

    ;; std::Option::Some::equal(Some left, Some right) -> i64

    (defn equal
        (left_addr right_addr)
        (native.i64.eq
            (builtin.memory.read_i64 left_addr 0)
            (builtin.memory.read_i64 right_addr 0)
        )
    )
)
