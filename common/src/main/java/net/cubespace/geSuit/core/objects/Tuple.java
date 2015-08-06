package net.cubespace.geSuit.core.objects;

public class Tuple<A, B> {
    private A a;
    private B b;
    
    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }
    
    public A getA() {
        return a;
    }
    
    public void setA(A value) {
        a = value;
    }
    
    public B getB() {
        return b;
    }
    
    public void setB(B value) {
        b = value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tuple<?, ?>)) {
            return false;
        }
        
        Tuple<?, ?> other = (Tuple<?,?>)obj;
        
        if (a != null) {
            if (!a.equals(other.a)) {
                return false;
            }
        } else {
            if (other.a != null) {
                return false;
            }
        }
        
        if (b != null) {
            return b.equals(other.b);
        } else {
            return other.b == null;
        }
    }
    
    @Override
    public int hashCode() {
        int v1, v2;
        
        if (a != null) {
            v1 = a.hashCode();
        } else {
            v1 = 0;
        }
        
        if (b != null) {
            v2 = b.hashCode();
        } else {
            v2 = 0;
        }
        
        return v1 ^ v2;
    }
}
