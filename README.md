# CustomedView

## viewGroup 的开发流程

必须实现的2个方法：onMeasure() -> onLayout();

### onMeasure()

onMeasure()是度量view的宽高的方法，规定的流程都是先度量子View的宽高，再依据子View的度量再去度量viewGroup自身的宽高。这个流程比较考验开发对viewGroup特性的理解，比如FrameLayout，所有的子View都是一层一层地叠在viewGroup中，所以viewGroup的宽高就是子View中最大的宽高；比如FlowLayout流式布局，宽度是最长的一行的宽度，高度是所有行的高度加其中的间隔。这些是要参透viewGroup的特性的，需要大量的练习。

#### 要注意的点：

1. LayoutParams、MeasureSpec各是什么，它们之间有什么练习？
   * LayoutParams 是view在xml中或者代码中设置的宽高属性。如特定的dp/px,match_parent, wrap_content这些。
   * MeasureSpec 是一个二进制的计算模型。它本身是一个int型的数据，利用int32位的特性，在最高的2位作为dp/px、match_parent、wrap_content的标识，对应着EXACITY,AT_MOST以及UNSPECIFIED（在数据上对应1,2,0）；后30位用于表示这个view实际的宽高。
   * 在onMeasure子View时，需要综合考虑LayoutParams和MeasureSpec的关系进行计算。(todo 需要补充一个测量模式的关系图)

2. 后续补充...

### onLayout()

onLayout()方法则是确定子View应该在viewGroup中的哪个位置的方法。

#### 要注意的点

1. 这个onLayout参考的坐标系是viewGroup本身，其原点为viewGroup的左上角。
2. view.layout(l,t,r,b)中各个参数的含义：
   * l:即是left，是子view的左边距离坐标系左边的距离
   * t:即是top，是子view的上边距离坐标系上边的距离
   * r:即是right，是子view的右边距离坐标系**左边**的距离
   * b:即是bottom，是子view的底边距离坐标系**上边**的距离
