Sequel.migration do
  up do
    create_table(:group_permissions) do
      primary_key [:permission, :group_id]

      String :permission, :size => 200
      foreign_key :group_id, :groups, :foreign_key_constraint_name => 'group_permissions_fkey_group_id'
      DateTime :updated_at
      DateTime :created_at
    end
  end

  down do
    drop_table(:group_permissions)
  end
end
