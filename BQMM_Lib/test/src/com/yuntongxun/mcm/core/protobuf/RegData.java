// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: RegData.proto

package com.yuntongxun.mcm.core.protobuf;

public final class RegData {
  private RegData() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }
  public interface RegDataInnerOrBuilder extends
      // @@protoc_insertion_point(interface_extends:RegDataInner)
      com.google.protobuf.MessageLiteOrBuilder {

    /**
     * <code>required string user = 1;</code>
     */
    boolean hasUser();
    /**
     * <code>required string user = 1;</code>
     */
    java.lang.String getUser();
    /**
     * <code>required string user = 1;</code>
     */
    com.google.protobuf.ByteString
        getUserBytes();

    /**
     * <code>required string password = 2;</code>
     */
    boolean hasPassword();
    /**
     * <code>required string password = 2;</code>
     */
    java.lang.String getPassword();
    /**
     * <code>required string password = 2;</code>
     */
    com.google.protobuf.ByteString
        getPasswordBytes();

    /**
     * <code>required uint32 expires = 3;</code>
     */
    boolean hasExpires();
    /**
     * <code>required uint32 expires = 3;</code>
     */
    int getExpires();
  }
  /**
   * Protobuf type {@code RegDataInner}
   */
  public static final class RegDataInner extends
      com.google.protobuf.GeneratedMessageLite implements
      // @@protoc_insertion_point(message_implements:RegDataInner)
      RegDataInnerOrBuilder {
    // Use RegDataInner.newBuilder() to construct.
    private RegDataInner(com.google.protobuf.GeneratedMessageLite.Builder builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private RegDataInner(boolean noInit) { this.unknownFields = com.google.protobuf.ByteString.EMPTY;}

    private static final RegDataInner defaultInstance;
    public static RegDataInner getDefaultInstance() {
      return defaultInstance;
    }

    public RegDataInner getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.ByteString unknownFields;
    private RegDataInner(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.ByteString.Output unknownFieldsOutput =
          com.google.protobuf.ByteString.newOutput();
      com.google.protobuf.CodedOutputStream unknownFieldsCodedOutput =
          com.google.protobuf.CodedOutputStream.newInstance(
              unknownFieldsOutput);
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFieldsCodedOutput,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000001;
              user_ = bs;
              break;
            }
            case 18: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000002;
              password_ = bs;
              break;
            }
            case 24: {
              bitField0_ |= 0x00000004;
              expires_ = input.readUInt32();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        try {
          unknownFieldsCodedOutput.flush();
        } catch (java.io.IOException e) {
        // Should not happen
        } finally {
          unknownFields = unknownFieldsOutput.toByteString();
        }
        makeExtensionsImmutable();
      }
    }
    public static com.google.protobuf.Parser<RegDataInner> PARSER =
        new com.google.protobuf.AbstractParser<RegDataInner>() {
      public RegDataInner parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new RegDataInner(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<RegDataInner> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int USER_FIELD_NUMBER = 1;
    private java.lang.Object user_;
    /**
     * <code>required string user = 1;</code>
     */
    public boolean hasUser() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required string user = 1;</code>
     */
    public java.lang.String getUser() {
      java.lang.Object ref = user_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          user_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string user = 1;</code>
     */
    public com.google.protobuf.ByteString
        getUserBytes() {
      java.lang.Object ref = user_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        user_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int PASSWORD_FIELD_NUMBER = 2;
    private java.lang.Object password_;
    /**
     * <code>required string password = 2;</code>
     */
    public boolean hasPassword() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>required string password = 2;</code>
     */
    public java.lang.String getPassword() {
      java.lang.Object ref = password_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          password_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string password = 2;</code>
     */
    public com.google.protobuf.ByteString
        getPasswordBytes() {
      java.lang.Object ref = password_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        password_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int EXPIRES_FIELD_NUMBER = 3;
    private int expires_;
    /**
     * <code>required uint32 expires = 3;</code>
     */
    public boolean hasExpires() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>required uint32 expires = 3;</code>
     */
    public int getExpires() {
      return expires_;
    }

    private void initFields() {
      user_ = "";
      password_ = "";
      expires_ = 0;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      if (!hasUser()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasPassword()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasExpires()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, getUserBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getPasswordBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeUInt32(3, expires_);
      }
      output.writeRawBytes(unknownFields);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(1, getUserBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, getPasswordBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt32Size(3, expires_);
      }
      size += unknownFields.size();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    /**
     * Protobuf type {@code RegDataInner}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageLite.Builder<
          com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner, Builder>
        implements
        // @@protoc_insertion_point(builder_implements:RegDataInner)
        com.yuntongxun.mcm.core.protobuf.RegData.RegDataInnerOrBuilder {
      // Construct using com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private void maybeForceBuilderInitialization() {
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        user_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        password_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        expires_ = 0;
        bitField0_ = (bitField0_ & ~0x00000004);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner getDefaultInstanceForType() {
        return com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner.getDefaultInstance();
      }

      public com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner build() {
        com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner buildPartial() {
        com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner result = new com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.user_ = user_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.password_ = password_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.expires_ = expires_;
        result.bitField0_ = to_bitField0_;
        return result;
      }

      public Builder mergeFrom(com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner other) {
        if (other == com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner.getDefaultInstance()) return this;
        if (other.hasUser()) {
          bitField0_ |= 0x00000001;
          user_ = other.user_;
          
        }
        if (other.hasPassword()) {
          bitField0_ |= 0x00000002;
          password_ = other.password_;
          
        }
        if (other.hasExpires()) {
          setExpires(other.getExpires());
        }
        setUnknownFields(
            getUnknownFields().concat(other.unknownFields));
        return this;
      }

      public final boolean isInitialized() {
        if (!hasUser()) {
          
          return false;
        }
        if (!hasPassword()) {
          
          return false;
        }
        if (!hasExpires()) {
          
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private java.lang.Object user_ = "";
      /**
       * <code>required string user = 1;</code>
       */
      public boolean hasUser() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required string user = 1;</code>
       */
      public java.lang.String getUser() {
        java.lang.Object ref = user_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            user_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string user = 1;</code>
       */
      public com.google.protobuf.ByteString
          getUserBytes() {
        java.lang.Object ref = user_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          user_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string user = 1;</code>
       */
      public Builder setUser(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        user_ = value;
        
        return this;
      }
      /**
       * <code>required string user = 1;</code>
       */
      public Builder clearUser() {
        bitField0_ = (bitField0_ & ~0x00000001);
        user_ = getDefaultInstance().getUser();
        
        return this;
      }
      /**
       * <code>required string user = 1;</code>
       */
      public Builder setUserBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        user_ = value;
        
        return this;
      }

      private java.lang.Object password_ = "";
      /**
       * <code>required string password = 2;</code>
       */
      public boolean hasPassword() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>required string password = 2;</code>
       */
      public java.lang.String getPassword() {
        java.lang.Object ref = password_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            password_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string password = 2;</code>
       */
      public com.google.protobuf.ByteString
          getPasswordBytes() {
        java.lang.Object ref = password_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          password_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string password = 2;</code>
       */
      public Builder setPassword(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        password_ = value;
        
        return this;
      }
      /**
       * <code>required string password = 2;</code>
       */
      public Builder clearPassword() {
        bitField0_ = (bitField0_ & ~0x00000002);
        password_ = getDefaultInstance().getPassword();
        
        return this;
      }
      /**
       * <code>required string password = 2;</code>
       */
      public Builder setPasswordBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        password_ = value;
        
        return this;
      }

      private int expires_ ;
      /**
       * <code>required uint32 expires = 3;</code>
       */
      public boolean hasExpires() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>required uint32 expires = 3;</code>
       */
      public int getExpires() {
        return expires_;
      }
      /**
       * <code>required uint32 expires = 3;</code>
       */
      public Builder setExpires(int value) {
        bitField0_ |= 0x00000004;
        expires_ = value;
        
        return this;
      }
      /**
       * <code>required uint32 expires = 3;</code>
       */
      public Builder clearExpires() {
        bitField0_ = (bitField0_ & ~0x00000004);
        expires_ = 0;
        
        return this;
      }

      // @@protoc_insertion_point(builder_scope:RegDataInner)
    }

    static {
      defaultInstance = new RegDataInner(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:RegDataInner)
  }


  static {
  }

  // @@protoc_insertion_point(outer_class_scope)
}
