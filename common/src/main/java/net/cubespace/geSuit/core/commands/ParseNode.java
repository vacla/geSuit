package net.cubespace.geSuit.core.commands;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

class ParseNode {
    private List<ParseNode> children;
    private ParseNode parent;
    private int variant;
    private int argumentIndex;
    private Function<String, ?> transformer;
    private boolean isTerminal;
    private boolean isVarArgs;
    
    public ParseNode(int variant, int argumentIndex, Function<String, ?> transformer, boolean varArgs) {
        this.variant = variant;
        this.argumentIndex = argumentIndex;
        this.transformer = transformer;
        this.isVarArgs = varArgs;
        
        children = Lists.newArrayList();
    }
    
    public ParseNode(int variant) {
        this.variant = variant;
        isTerminal = true;
    }
    
    public void addChild(ParseNode node) {
        Preconditions.checkArgument(node.parent == null);
        
        int score = node.getScore();
        boolean inserted = false;
        
        // Order as normal, varargs, terminal
        for (int i = 0; i < children.size(); ++i) {
            int otherScore = children.get(i).getScore();
            if (score < otherScore) {
                children.add(i, node);
                inserted = true;
            }
        }
        
        if (!inserted) {
            children.add(node);
        }
        
        node.parent = this;
    }
    
    private int getScore() {
        if (isTerminal) {
            return 2;
        } else if (isVarArgs) {
            return 1;
        } else {
            return 0;
        }
    }
    
    public List<ParseNode> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public ParseNode getParent() {
        return parent;
    }
    
    public boolean isTerminal() {
        return isTerminal;
    }
    
    public int getVariant() {
        return variant;
    }
    
    public boolean isVarArgs() {
        return isVarArgs;
    }
    
    public int getArgumentIndex() {
        return argumentIndex;
    }
    
    public Object parse(String value) {
        return transformer.apply(value);
    }
    
    @Override
    public String toString() {
        String text;
        if (isTerminal) {
            text = "*term*";
        } else {
            text = String.valueOf(transformer);
            if (isVarArgs) {
                text += "...";
            }
        }
        return String.format("%s:%s", text, children);
    }
}
