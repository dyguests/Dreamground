# Dreamground

Dreamground.一些动态的背景自定义控件。

![](/graphics/ripple.gif | width=48)

# Usage

    <declare-styleable name="RippleView">
        <attr name="bgColor" format="color" />
        <attr name="rippleType" format="enum">
            <enum name="ripple" value="0" />
            <enum name="drop" value="1" />
        </attr>
        <attr name="rippleColor" format="color" />
        <attr name="rippleRadius" format="dimension" />
        <attr name="rippleRadiusFluctuation" format="float" />
        <attr name="rippleLifetime" format="integer" />
        <attr name="rippleIncubateInterval" format="integer" />
        <attr name="rippleIncubateIntervalFluctuation" format="float" />
    </declare-styleable>

# TODO

- [ ] onPause process.